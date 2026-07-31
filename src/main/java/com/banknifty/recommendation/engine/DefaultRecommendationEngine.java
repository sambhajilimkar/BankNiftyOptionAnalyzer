package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.IndicatorPipeline;
import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskLevel;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;
import com.banknifty.optionchain.history.OptionSnapshotHistoryService;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.service.OptionSnapshotService;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.service.OptionChainAnalyzer;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Produces a trade only when technical momentum and full option-chain analysis
 * agree. The option-chain snapshot supplies PCR, max pain, gamma exposure, OI
 * build-up, support/resistance, liquidity and Greeks to contract ranking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRecommendationEngine implements RecommendationEngine {

	private static final String NSE = "NSE";
	private static final String INTERVAL = "5minute";
	private static final int HISTORY_DAYS = 10;
	private static final int MINIMUM_TECHNICAL_CONFIDENCE = 60;

	private final IndicatorPipeline indicatorPipeline;
	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteInstrumentProvider instrumentProvider;
	private final KiteQuoteProvider quoteProvider;
	private final OptionUniverseService optionUniverseService;
	private final TradingProperties tradingProperties;
	private final OptionSnapshotService optionSnapshotService;
	private final OptionSnapshotHistoryService snapshotHistoryService;
	private final InstitutionalAnalysisEngine institutionalAnalysisEngine;
	private final OptionChainAnalyzer optionChainAnalyzer;
	private final OptionAnalysisEngine optionAnalysisEngine;
	private final RankingEngine rankingEngine;

	@Override
	public TradeRecommendation recommend(RecommendationRequest request) {
		RecommendationRequest normalized = normalize(request);
		String spotSymbol = spotSymbol(normalized.instrument());
		BigDecimal spotPrice = liveSpotPrice(spotSymbol);
		IndicatorSnapshot indicators = indicatorPipeline.calculate(historicalCandles(spotSymbol));
		Signal technicalSignal = signal(indicators);

		OptionSnapshot snapshot = optionSnapshotService.getLatestSnapshot(normalized, spotPrice);
		if (snapshot == null) {
			return noTrade(normalized, spotPrice, technicalSignal, null,
					"Live option-chain snapshot is unavailable for the selected expiry");
		}
		OptionSnapshot previousSnapshot = snapshotHistoryService
				.latestMatching(snapshot.underlying(), snapshot.expiry()).orElse(null);

		InstitutionalAnalysis institutional = institutionalAnalysisEngine.analyze(snapshot, previousSnapshot,
				AnalysisContext.builder().spotPrice(spotPrice).build());
		snapshotHistoryService.save(snapshot);
		if (technicalSignal.confidence() < MINIMUM_TECHNICAL_CONFIDENCE) {
			return noTrade(normalized, spotPrice, technicalSignal, institutional,
					"Technical confirmation is below " + MINIMUM_TECHNICAL_CONFIDENCE + "%");
		}

		int overallConfidence = (int) Math
				.round((technicalSignal.confidence() * 0.60) + (institutional.getConfidence() * 0.40));

		if (overallConfidence < 55) {
			return noTrade(normalized, spotPrice, technicalSignal, institutional,
					"Combined technical + institutional confidence is below 55%", overallConfidence);
		}

		if (institutionalDisagrees(technicalSignal.optionType(), institutional)) {
			return noTrade(normalized, spotPrice, technicalSignal, institutional,
					"Technical trend and institutional option-chain bias disagree", overallConfidence);
		}

		AnalysisContext context = AnalysisContext.builder().spotPrice(spotPrice)
				.marketBias(marketBias(technicalSignal.optionType(), technicalSignal.confidence()))
				.trendScore(technicalSignal.optionType() == OptionType.CE ? technicalSignal.confidence()
						: -technicalSignal.confidence())
				.confidence(technicalSignal.confidence()).institutionalAnalysis(institutional).build();

		List<OptionQuote> quotes = optionUniverseService.loadUniverse(normalized);
		List<OptionAnalysis> ranked = rankingEngine.top(optionChainAnalyzer.analyze(quotes, spotPrice).stream()
				.map(candidate -> optionAnalysisEngine.analyze(context, candidate)).toList(), context, 5);

		if (ranked.isEmpty()) {
			return noTrade(normalized, spotPrice, technicalSignal, institutional,
					"No liquid contract matched the selected expiry, risk profile, and trading style");
		}

		OptionAnalysis best = ranked.getFirst();
		best.addReason(contractPreferenceReason(normalized));
		return trade(normalized, spotPrice, technicalSignal, institutional, best);
	}

	private TradeRecommendation trade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, OptionAnalysis best) {
		List<String> reasons = new ArrayList<>(signal.reasons());
		reasons.addAll(best.getReasons());
		reasons.add("Full option-chain institutional analysis confirmed the selected contract");
		int confidence = (int) Math
				.round(Math.min(100, (signal.confidence() * 0.55) + (institutional.getConfidence() * 0.45)));
		TradeLevels levels = tradeLevels(best.getEntry(), request.tradingStyle(), request.riskProfile());

		return TradeRecommendation.builder().action(RecommendationAction.BUY).instrument(request.instrument())
				.expiryDate(best.getCandidate().getExpiry()).expiryLabel(request.expiryType().name())
				.optionType(best.getCandidate().getOptionType()).strikePrice(best.getCandidate().getStrike())
				.spotPrice(spotPrice).optionPrice(best.getCandidate().getPremium()).entryMin(best.getEntry())
				.entryMax(best.getEntry()).stopLoss(levels.stopLoss()).target1(levels.target1())
				.target2(levels.target2()).target3(levels.target3()).confidence(confidence)
				.risk(riskLevel(request.riskProfile())).quantity(positionLots(request.capital(), best.getEntry(), request.riskProfile()))
				.holdingTime(holdingTime(request.tradingStyle())).reasons(List.copyOf(reasons))
				.rejectedReasons(List.of()).institutionalAnalysis(institutional)
				.technicalConfidence(signal.confidence()).technicalBias(marketBias(signal.optionType(), signal.confidence())).build();
	}

	private boolean institutionalDisagrees(OptionType technicalDirection, InstitutionalAnalysis institutional) {
		if (institutional == null || institutional.getMarketBias() == null
				|| institutional.getMarketBias() == MarketBias.SIDEWAYS) {
			return false;
		}
		return (technicalDirection == OptionType.CE && (institutional.getMarketBias() == MarketBias.BEARISH
				|| institutional.getMarketBias() == MarketBias.STRONG_BEARISH))
				|| (technicalDirection == OptionType.PE && (institutional.getMarketBias() == MarketBias.BULLISH
						|| institutional.getMarketBias() == MarketBias.STRONG_BULLISH));
	}

	private MarketBias marketBias(OptionType type, int confidence) {
		if (type == OptionType.CE) {
			return confidence >= 80 ? MarketBias.STRONG_BULLISH : MarketBias.BULLISH;
		}
		return confidence >= 80 ? MarketBias.STRONG_BEARISH : MarketBias.BEARISH;
	}

	private Signal signal(IndicatorSnapshot indicators) {
		int bullish = 0;
		int bearish = 0;
		List<String> bullishReasons = new ArrayList<>();
		List<String> bearishReasons = new ArrayList<>();
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
		return bullish >= bearish ? new Signal(OptionType.CE, Math.min(bullish, 95), bullishReasons)
				: new Signal(OptionType.PE, Math.min(bearish, 95), bearishReasons);
	}

	private TradeRecommendation noTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, String rejectedReason) {
		return noTrade(request, spotPrice, signal, institutional, rejectedReason, signal.confidence());
	}

	private TradeRecommendation noTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, String rejectedReason, int recommendationConfidence) {
		return TradeRecommendation.builder().action(RecommendationAction.WAIT).instrument(request.instrument())
				.expiryLabel(request.expiryType().name()).spotPrice(spotPrice).optionType(null)
				.confidence(recommendationConfidence).risk(riskLevel(request.riskProfile())).quantity(0)
				.holdingTime("No trade").reasons(List.copyOf(signal.reasons())).rejectedReasons(List.of(rejectedReason))
				.institutionalAnalysis(institutional).technicalConfidence(signal.confidence())
				.technicalBias(marketBias(signal.optionType(), signal.confidence())).build();
	}

	private List<Candle> historicalCandles(String symbol) {
		LocalDateTime to = LocalDateTime.now();
		try {
			return historicalDataProvider.fetchHistoricalData(instrumentProvider.getInstrumentToken(NSE, symbol),
					symbol, NSE, INTERVAL, to.minusDays(HISTORY_DAYS), to, false, false);
		} catch (Exception | KiteException exception) {
			throw new IllegalStateException("Unable to load Zerodha historical candles for " + symbol, exception);
		}
	}

	private BigDecimal liveSpotPrice(String symbol) {
		BigDecimal ltp = quoteProvider.getLTP(NSE + ":" + symbol);
		if (ltp == null || ltp.signum() <= 0)
			throw new IllegalStateException("No live index price available for " + symbol);
		return ltp;
	}

	private RecommendationRequest normalize(RecommendationRequest request) {
		if (request == null || request.instrument() == null || request.instrument().isBlank())
			throw new IllegalArgumentException("Instrument is required");
		return RecommendationRequest.builder().instrument(optionUnderlying(request.instrument()))
				.expiryType(request.expiryType() == null ? com.banknifty.enums.ExpiryType.WEEKLY : request.expiryType())
				.tradingStyle(request.tradingStyle() == null ? TradingStyle.INTRADAY : request.tradingStyle())
				.riskProfile(request.riskProfile() == null ? RiskProfile.BALANCED : request.riskProfile())
				.capital(request.capital()).build();
	}

	private String spotSymbol(String instrument) {
		return "BANKNIFTY".equalsIgnoreCase(instrument) ? "NIFTY BANK" : instrument;
	}

	private String optionUnderlying(String instrument) {
		String normalized = instrument.trim().toUpperCase();
		return normalized.equals("NIFTY BANK") || normalized.equals("BANK NIFTY") ? "BANKNIFTY" : normalized;
	}

	private int positionLots(Double capital, BigDecimal entry) {
		return positionLots(capital, entry, RiskProfile.BALANCED);
	}

	private int positionLots(Double capital, BigDecimal entry, RiskProfile profile) {
		if (capital == null || capital <= 0 || entry == null || entry.signum() <= 0)
			return 1;
		double costPerLot = entry.doubleValue() * Math.max(1, tradingProperties.getLotSize());
		int affordableLots = (int) Math.floor(capital / costPerLot);
		return Math.max(1, Math.min(maxLots(profile), affordableLots));
	}

	private boolean matchesTradePreferences(com.banknifty.recommendation.model.OptionCandidate candidate,
			RecommendationRequest request) {
		int distance = candidate.getStrikeDistance() == null ? Integer.MAX_VALUE : candidate.getStrikeDistance();
		return switch (request.tradingStyle()) {
		case SCALPING -> candidate.isAtm();
		case INTRADAY -> candidate.isAtm() || (request.riskProfile() == RiskProfile.AGGRESSIVE && distance <= 1);
		case SWING -> candidate.isItm() && distance <= preferredItmDistance(request.riskProfile(), 2);
		case POSITIONAL -> candidate.isItm() && distance <= preferredItmDistance(request.riskProfile(), 3);
		};
	}

	private int preferredItmDistance(RiskProfile profile, int baseDistance) {
		return switch (profile) {
		case CONSERVATIVE -> 1;
		case MODERATE, BALANCED -> baseDistance;
		case AGGRESSIVE -> baseDistance + 1;
		};
	}

	private String contractPreferenceReason(RecommendationRequest request) {
		return switch (request.tradingStyle()) {
		case SCALPING -> "ATM contract selected for scalping liquidity";
		case INTRADAY -> "Near-ATM contract selected for intraday liquidity";
		case SWING -> "ITM contract selected for swing-trade delta and time-value protection";
		case POSITIONAL -> "ITM contract selected for positional-trade delta and time-value protection";
		};
	}

	private TradeLevels tradeLevels(BigDecimal entry, TradingStyle style, RiskProfile profile) {
		double[] styleMultipliers = switch (style) {
		case SCALPING -> new double[] { 0.95, 1.08, 1.15, 1.22 };
		case INTRADAY -> new double[] { 0.90, 1.12, 1.25, 1.40 };
		case SWING -> new double[] { 0.85, 1.20, 1.40, 1.60 };
		case POSITIONAL -> new double[] { 0.80, 1.30, 1.60, 2.00 };
		};
		double targetAdjustment = switch (profile) {
		case CONSERVATIVE -> -0.03;
		case MODERATE -> -0.01;
		case BALANCED -> 0;
		case AGGRESSIVE -> 0.05;
		};
		double stopAdjustment = switch (profile) {
		case CONSERVATIVE -> 0.03;
		case MODERATE -> 0.01;
		case BALANCED -> 0;
		case AGGRESSIVE -> -0.03;
		};
		return new TradeLevels(level(entry, styleMultipliers[0] + stopAdjustment),
				level(entry, styleMultipliers[1] + targetAdjustment),
				level(entry, styleMultipliers[2] + targetAdjustment),
				level(entry, styleMultipliers[3] + targetAdjustment));
	}

	private int maxLots(RiskProfile profile) {
		return switch (profile) {
		case CONSERVATIVE -> 1;
		case MODERATE -> 2;
		case BALANCED -> 3;
		case AGGRESSIVE -> 5;
		};
	}

	private String holdingTime(TradingStyle style) {
		return switch (style) {
		case SCALPING -> "5-15 minutes";
		case INTRADAY -> "30-90 minutes";
		case SWING -> "1-3 trading days";
		case POSITIONAL -> "1-4 weeks";
		};
	}

	private RiskLevel riskLevel(RiskProfile profile) {
		return switch (profile) {
		case CONSERVATIVE -> RiskLevel.LOW;
		case BALANCED, MODERATE -> RiskLevel.MEDIUM;
		case AGGRESSIVE -> RiskLevel.HIGH;
		};
	}

	private BigDecimal level(BigDecimal value, double multiplier) {
		return value.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
	}

	private record Signal(OptionType optionType, int confidence, List<String> reasons) {
	}

	private record TradeLevels(BigDecimal stopLoss, BigDecimal target1, BigDecimal target2, BigDecimal target3) {
	}
}
