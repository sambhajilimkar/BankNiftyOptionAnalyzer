package com.banknifty.service.impl;

import com.banknifty.engine.RecommendationEngine;
import com.banknifty.engine.TrendScoreEngine;
import com.banknifty.market.MarketDataService;
import com.banknifty.model.Candle;
import com.banknifty.model.OptionRecommendation;
import com.banknifty.model.TrendScore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisServiceImpl {

	private final MarketDataService marketDataService;
	private final TrendScoreEngine trendScoreEngine;
	private final RecommendationEngine recommendationEngine;

	public AnalysisServiceImpl(MarketDataService marketDataService, TrendScoreEngine trendScoreEngine,
			RecommendationEngine recommendationEngine) {

		this.marketDataService = marketDataService;
		this.trendScoreEngine = trendScoreEngine;
		this.recommendationEngine = recommendationEngine;
	}

	/**
	 * Analyze one instrument and return recommendation.
	 */
	public OptionRecommendation analyze(Long instrumentToken, String tradingSymbol, String exchange, String interval,
			LocalDateTime from, LocalDateTime to) {

		List<Candle> candles = marketDataService.getHistoricalCandles(instrumentToken, tradingSymbol, exchange,
				interval, from, to);

		TrendScore trendScore = trendScoreEngine.calculate(candles);

		return recommendationEngine.recommend(trendScore);
	}
}