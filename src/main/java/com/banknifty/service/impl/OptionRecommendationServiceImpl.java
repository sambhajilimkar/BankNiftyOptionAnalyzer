package com.banknifty.service.impl;

import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.model.Candle;
import com.banknifty.model.Recommendation;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.recommendation.engine.OptionAnalysisEngine;
import com.banknifty.recommendation.engine.RankingEngine;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;
import com.banknifty.recommendation.model.RankedOption;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.service.OptionChainAnalyzer;
import com.banknifty.service.OptionRecommendationService;
import com.banknifty.service.TrendAnalysisResult;
import com.banknifty.service.TrendAnalysisService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptionRecommendationServiceImpl implements OptionRecommendationService {

	private static final String EXCHANGE = "NSE";
	private static final String INTERVAL = "5minute";
	private static final int HISTORY_DAYS = 7;
	private static final int TOP_RANKED_CONTRACTS = 5;

	private final TrendAnalysisService trendAnalysisService;
	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteInstrumentProvider instrumentProvider;
	private final KiteQuoteProvider quoteProvider;
	private final OptionUniverseService optionUniverseService;
	private final OptionChainAnalyzer optionChainAnalyzer;
	private final OptionAnalysisEngine optionAnalysisEngine;
	private final RankingEngine rankingEngine;

	@Override
	public Recommendation recommend(String underlying, ExpiryType expiryType) {

		String spotSymbol = spotSymbol(underlying);
		String optionUnderlying = optionUnderlying(underlying);
		BigDecimal liveSpotPrice = liveIndexPrice(spotSymbol);
		TrendAnalysisResult trend = trendAnalysisService.analyze(historicalCandles(spotSymbol));
		AnalysisContext context = analysisContext(trend, liveSpotPrice);

		List<OptionQuote> universe = optionUniverseService
				.loadUniverse(recommendationRequest(optionUnderlying, expiryType));
		List<OptionCandidate> candidates = optionChainAnalyzer.analyze(universe, liveSpotPrice);
		List<OptionAnalysis> analyses = candidates.stream()
				.map(candidate -> optionAnalysisEngine.analyze(context, candidate)).toList();
		log.info("Candidates received : {}", candidates.size());
		log.info("Analyses created    : {}", analyses.size());

		for (OptionAnalysis analysis : analyses) {

			log.info("Strike={} Type={} Score={} Confidence={}", analysis.getCandidate().getStrike(),
					analysis.getCandidate().getOptionType(), analysis.getTotalScore(), analysis.getConfidence());
		}
		List<OptionAnalysis> ranked = rankingEngine.top(analyses, context, TOP_RANKED_CONTRACTS);
		log.info("Ranked Contracts : {}", ranked.size());

		for (OptionAnalysis analysis : ranked) {

			log.info("RANK {} {} Score={}", analysis.getCandidate().getStrike(),
					analysis.getCandidate().getOptionType(), analysis.getTotalScore());
		}
		if (ranked.isEmpty()) {
			return noTrade(trend, liveSpotPrice, context.getMarketBias(),
					"No tradable option contracts found in the selected option chain");
		}
		if (ranked.isEmpty()) {

			log.error("RankingEngine returned ZERO contracts.");

		} else {

			OptionAnalysis best = ranked.get(0);

			log.info("BEST CONTRACT : {} {} Score={}", best.getCandidate().getStrike(),
					best.getCandidate().getOptionType(), best.getTotalScore());
		}

		return recommendation(trend, liveSpotPrice, context.getMarketBias(), ranked);
	}

	private RecommendationRequest recommendationRequest(String underlying, ExpiryType expiryType) {

		return RecommendationRequest.builder().instrument(underlying).expiryType(expiryType)
				.tradingStyle(TradingStyle.INTRADAY).riskProfile(RiskProfile.BALANCED).capital(null).build();
	}

	private AnalysisContext analysisContext(TrendAnalysisResult trend, BigDecimal spotPrice) {

		return AnalysisContext.builder().spotPrice(spotPrice).marketBias(marketBias(trend))
				.trendScore(trendScore(trend)).confidence(trend.confidence()).build();
	}

	private MarketBias marketBias(TrendAnalysisResult trend) {

		if (trend.action() != RecommendationAction.BUY) {
			return MarketBias.SIDEWAYS;
		}

		if (trend.optionType() == OptionType.CE) {
			return trend.confidence() >= 80 ? MarketBias.STRONG_BULLISH : MarketBias.BULLISH;
		}

		return trend.confidence() >= 80 ? MarketBias.STRONG_BEARISH : MarketBias.BEARISH;
	}

	private int trendScore(TrendAnalysisResult trend) {

		if (trend.action() != RecommendationAction.BUY) {
			return 0;
		}

		return trend.optionType() == OptionType.CE ? trend.confidence() : -trend.confidence();
	}

	private Recommendation recommendation(

			TrendAnalysisResult trend,

			BigDecimal spotPrice,

			MarketBias marketBias,

			List<OptionAnalysis> ranked) {

		OptionAnalysis best = ranked.getFirst();
		OptionCandidate candidate = best.getCandidate();
		List<String> reasons = new ArrayList<>(trend.reasons());
		reasons.addAll(best.getReasons());
		reasons.add("Selected from complete option-chain ranking");
		reasons.add("Rank : " + best.getRank());
		reasons.add("Composite Score : " + best.getTotalScore());

		return Recommendation.builder().action(RecommendationAction.BUY).optionType(candidate.getOptionType())
				.tradingSymbol(candidate.getTradingSymbol()).strike(candidate.getStrike()).spotPrice(spotPrice)
				.entry(best.getEntry()).stopLoss(best.getStopLoss()).target1(best.getTarget1())
				.target2(best.getTarget2()).confidence((int) Math.round(best.getConfidence()))
				.reasons(List.copyOf(reasons)).marketBias(marketBias)
				.topRankedContracts(ranked.stream().map(this::rankedOption).toList()).build();
	}

	private RankedOption rankedOption(OptionAnalysis analysis) {

		return RankedOption.builder().rank(analysis.getRank()).candidate(analysis.getCandidate())
				.entry(analysis.getEntry()).stopLoss(analysis.getStopLoss()).target1(analysis.getTarget1())
				.target2(analysis.getTarget2()).totalScore(analysis.getTotalScore())
				.confidence(analysis.getConfidence()).reasons(List.copyOf(analysis.getReasons())).build();
	}

	private Recommendation noTrade(

			TrendAnalysisResult trend,

			BigDecimal spotPrice,

			MarketBias marketBias,

			String reason) {

		List<String> reasons = new ArrayList<>(trend.reasons());
		reasons.add(reason);

		return Recommendation.builder().action(RecommendationAction.WAIT).optionType(trend.optionType())
				.tradingSymbol(null).strike(null).spotPrice(spotPrice).entry(BigDecimal.ZERO).stopLoss(BigDecimal.ZERO)
				.target1(BigDecimal.ZERO).target2(BigDecimal.ZERO).confidence(trend.confidence())
				.reasons(List.copyOf(reasons)).marketBias(marketBias).topRankedContracts(List.of()).build();
	}

	private List<Candle> historicalCandles(String symbol) {

		LocalDateTime to = LocalDateTime.now();

		try {
			return historicalDataProvider.fetchHistoricalData(instrumentProvider.getInstrumentToken(EXCHANGE, symbol),
					symbol, EXCHANGE, INTERVAL, to.minusDays(HISTORY_DAYS), to, false, false);
		} catch (Exception | KiteException ex) {
			throw new IllegalStateException("Unable to load historical candles for " + symbol, ex);
		}
	}

	private BigDecimal liveIndexPrice(String symbol) {

		try {
			BigDecimal ltp = quoteProvider.getLTP(EXCHANGE + ":" + symbol);

			if (ltp == null || ltp.signum() <= 0) {
				throw new IllegalStateException("Invalid LTP received");
			}

			return ltp;
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to fetch live price for " + symbol, ex);
		}
	}

	private String spotSymbol(String underlying) {

		if (underlying == null || underlying.isBlank()) {
			return "NIFTY BANK";
		}

		String symbol = underlying.trim().toUpperCase();

		return symbol.equals("BANKNIFTY") || symbol.equals("BANK NIFTY") ? "NIFTY BANK" : symbol;
	}

	private String optionUnderlying(String underlying) {

		if (underlying == null || underlying.isBlank()) {
			return "BANKNIFTY";
		}

		String symbol = underlying.trim().toUpperCase();

		return symbol.equals("NIFTY BANK") || symbol.equals("BANK NIFTY") ? "BANKNIFTY" : symbol;
	}
}
