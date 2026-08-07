package com.banknifty.recommendation.context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.IndicatorPipeline;
import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.analysis.prediction.PredictionAnalysis;
import com.banknifty.analysis.prediction.PredictionContext;
import com.banknifty.analysis.prediction.PredictionEngine;
import com.banknifty.analysis.reversal.MarketReversalEngine;
import com.banknifty.analysis.reversal.ReversalAnalysis;
import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;
import com.banknifty.market.context.MarketContextService;
import com.banknifty.market.regime.MarketRegimeEngine;
import com.banknifty.market.regime.MarketRegimeResult;
import com.banknifty.model.Candle;
import com.banknifty.optionchain.history.OptionSnapshotHistoryService;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.service.OptionSnapshotService;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.recommendation.engine.InstitutionalAnalysisEngine;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.Signal;
import com.banknifty.service.TrendAnalysisResult;
import com.banknifty.service.TrendAnalysisService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationContextFactory {

	private static final String NSE = "NSE";
	private static final String INTERVAL = "5minute";
	private static final int HISTORY_DAYS = 10;
	private static final int MINIMUM_TECHNICAL_CONFIDENCE = 60;

	private final IndicatorPipeline indicatorPipeline;
	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteInstrumentProvider instrumentProvider;
	private final KiteQuoteProvider quoteProvider;
	private final TradingProperties tradingProperties;
	private final OptionSnapshotService optionSnapshotService;
	private final OptionSnapshotHistoryService snapshotHistoryService;
	private final InstitutionalAnalysisEngine institutionalAnalysisEngine;
	private final MarketContextService marketContextService;
	private final MarketRegimeEngine marketRegimeEngine;
	private final TrendAnalysisService trendAnalysisService;
	private final MarketReversalEngine marketReversalEngine;
	private final PredictionEngine predictionEngine;

	public RecommendationContext build(RecommendationRequest request) {

		RecommendationRequest normalized = normalize(request);

		List<Candle> candles = historicalCandles(spotSymbol(normalized.instrument()));

		IndicatorSnapshot indicators = indicatorPipeline.calculate(candles);

		Signal signal = signal(indicators);

		TrendAnalysisResult structure = trendAnalysisService.analyze(candles);

		MarketContext marketContext = marketContextService.analyse();

		MarketRegimeResult regime = marketRegimeEngine.detect(indicators, marketContext);

		BigDecimal spot = liveSpotPrice(spotSymbol(normalized.instrument()));

		OptionSnapshot snapshot = optionSnapshotService.getLatestSnapshot(normalized, spot);

		InstitutionalAnalysis institutional = snapshot == null ? null
				: institutionalAnalysisEngine.analyze(snapshot,
						snapshotHistoryService.latestMatching(snapshot.underlying(), snapshot.expiry()).orElse(null),
						AnalysisContext.builder().spotPrice(spot).build());

		AnalysisContext analysisContext = AnalysisContext.builder().spotPrice(spot)
				.marketBias(marketBias(signal.getOptionType(), signal.getConfidence()))
				.trendScore(signal.getOptionType() == OptionType.CE ? signal.getConfidence() : -signal.getConfidence())
				.confidence(signal.getConfidence()).institutionalAnalysis(institutional).build();

		ReversalAnalysis reversal = marketReversalEngine.analyze(ReversalContext.builder().candles(candles)
				.indicators(indicators).analysisContext(analysisContext).build());

		PredictionAnalysis prediction = predictionEngine.predict(PredictionContext.builder().candles(candles)
				.indicators(indicators).analysisContext(analysisContext).build(), reversal);

		int combinedConfidence = (int) Math.round(
				(signal.getConfidence() * 0.60) + (institutional == null ? 0 : institutional.getConfidence() * 0.40));

		combinedConfidence -= (int) (reversal.getReversalProbability() * 0.25);

		combinedConfidence = Math.max(0, combinedConfidence);

		boolean entryAllowed = regime.tradeAllowed() && signal.getConfidence() >= MINIMUM_TECHNICAL_CONFIDENCE
				&& combinedConfidence >= 55 && !institutionalDisagrees(signal.getOptionType(), institutional);

		if (reversal.getReversalProbability() >= 75) {
			entryAllowed = false;
		}

		String gateReason = !regime.tradeAllowed() ? "Market regime/context blocks new entries: " + regimeReason(regime)
				: signal.getConfidence() < MINIMUM_TECHNICAL_CONFIDENCE
						? "Technical confirmation is below " + MINIMUM_TECHNICAL_CONFIDENCE + "%"
						: combinedConfidence < 55 ? "Combined technical + institutional confidence is below 55%"
								: reversal.getReversalProbability() >= 75
										? "High probability of market reversal ("
												+ Math.round(reversal.getReversalProbability()) + "%)"
										: institutionalDisagrees(signal.getOptionType(), institutional)
												? "Technical trend and institutional option-chain bias disagree"
												: "Awaiting ranking and trade setup validation";

		return RecommendationContext.builder().request(normalized).spot(spot).candles(candles).indicators(indicators)
				.signal(signal).structure(structure).marketContext(marketContext).regime(regime)
				.institutional(institutional).analysisContext(analysisContext).prediction(prediction).reversal(reversal)
				.combinedConfidence(combinedConfidence).entryAllowed(entryAllowed).gateReason(gateReason).build();
	}

	/*
	 * -----------------------------------------------------------------------
	 * Helper methods (Copied exactly from DefaultRecommendationEngine)
	 * -----------------------------------------------------------------------
	 */

	private List<Candle> historicalCandles(String symbol) {

		LocalDateTime to = LocalDateTime.now();

		try {

			return historicalDataProvider.fetchHistoricalData(

					instrumentProvider.getInstrumentToken(NSE, symbol),

					symbol,

					NSE,

					INTERVAL,

					to.minusDays(HISTORY_DAYS),

					to,

					false,

					false);

		} catch (Exception | KiteException exception) {

			throw new IllegalStateException(

					"Unable to load Zerodha historical candles for " + symbol,

					exception);
		}
	}

	private BigDecimal liveSpotPrice(String symbol) {

		BigDecimal ltp = quoteProvider.getLTP(NSE + ":" + symbol);

		if (ltp == null || ltp.signum() <= 0) {

			throw new IllegalStateException(

					"No live index price available for " + symbol);
		}

		return ltp;
	}

	private RecommendationRequest normalize(RecommendationRequest request) {

		if (request == null || request.instrument() == null || request.instrument().isBlank()) {

			throw new IllegalArgumentException("Instrument is required");
		}

		return RecommendationRequest.builder()

				.instrument(optionUnderlying(request.instrument()))

				.expiryType(request.expiryType() == null ? com.banknifty.enums.ExpiryType.WEEKLY : request.expiryType())

				.tradingStyle(request.tradingStyle() == null ? TradingStyle.INTRADAY : request.tradingStyle())

				.riskProfile(request.riskProfile() == null ? RiskProfile.BALANCED : request.riskProfile())

				.capital(request.capital())

				.build();
	}

	private String spotSymbol(String instrument) {

		return "BANKNIFTY".equalsIgnoreCase(instrument) ? "NIFTY BANK" : instrument;
	}

	private String optionUnderlying(String instrument) {

		String normalized = instrument.trim().toUpperCase();

		return normalized.equals("NIFTY BANK") || normalized.equals("BANK NIFTY") ? "BANKNIFTY" : normalized;
	}

	private boolean institutionalDisagrees(

			OptionType technicalDirection,

			InstitutionalAnalysis institutional) {

		if (institutional == null || institutional.getMarketBias() == null
				|| institutional.getMarketBias() == MarketBias.SIDEWAYS) {

			return false;
		}

		return (technicalDirection == OptionType.CE && (institutional.getMarketBias() == MarketBias.BEARISH
				|| institutional.getMarketBias() == MarketBias.STRONG_BEARISH))

				|| (technicalDirection == OptionType.PE && (institutional.getMarketBias() == MarketBias.BULLISH
						|| institutional.getMarketBias() == MarketBias.STRONG_BULLISH));
	}

	private MarketBias marketBias(

			OptionType type,

			int confidence) {

		if (type == OptionType.CE) {

			return confidence >= 80 ? MarketBias.STRONG_BULLISH : MarketBias.BULLISH;
		}

		return confidence >= 80 ? MarketBias.STRONG_BEARISH : MarketBias.BEARISH;
	}

	private String regimeReason(MarketRegimeResult regime) {

		return regime == null || regime.reasons() == null || regime.reasons().isEmpty()

				? "regime not confirmed"

				: regime.reasons().getFirst();
	}

	private Signal signal(IndicatorSnapshot indicators) {

		int bullish = 0;
		int bearish = 0;

		List<String> bullishReasons = new java.util.ArrayList<>();
		List<String> bearishReasons = new java.util.ArrayList<>();

		if (indicators.ema().bullishAlignment()) {
			bullish += 25;
			bullishReasons.add("EMA bullish alignment");
		}

		if (indicators.ema().bearishAlignment()) {
			bearish += 25;
			bearishReasons.add("EMA bearish alignment");
		}

		if (indicators.rsi().bullish() && indicators.rsi().rising()) {
			bullish += 15;
			bullishReasons.add("RSI rising");
		}

		if (indicators.rsi().bearish() && indicators.rsi().falling()) {
			bearish += 15;
			bearishReasons.add("RSI falling");
		}

		if (indicators.macd().bullish() || indicators.macd().bullishCross()) {
			bullish += 20;
			bullishReasons.add("MACD bullish");
		}

		if (indicators.macd().bearish() || indicators.macd().bearishCross()) {
			bearish += 20;
			bearishReasons.add("MACD bearish");
		}

		if (indicators.vwap().aboveVWAP()) {
			bullish += 15;
			bullishReasons.add("Price above VWAP");
		} else {
			bearish += 15;
			bearishReasons.add("Price below VWAP");
		}

		if (indicators.adx().strongTrend()) {

			if (indicators.adx().bullish()) {
				bullish += 15;
				bullishReasons.add("ADX confirms bullish trend");
			}

			if (indicators.adx().bearish()) {
				bearish += 15;
				bearishReasons.add("ADX confirms bearish trend");
			}
		}

		return bullish >= bearish

				? new Signal(OptionType.CE, Math.min(bullish, 95), bullishReasons)

				: new Signal(OptionType.PE, Math.min(bearish, 95), bearishReasons);
	}
}