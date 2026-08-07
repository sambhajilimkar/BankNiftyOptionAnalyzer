package com.banknifty.service;

import com.banknifty.engine.TrendScoreEngine;
import com.banknifty.market.MarketDataService;
import com.banknifty.recommendation.engine.RecommendationEngine;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.RecommendationResponse;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnalysisService {

	private final RecommendationEngine recommendationEngine;

	public AnalysisService(MarketDataService marketDataService, TrendScoreEngine trendScoreEngine,
			RecommendationEngine recommendationEngine) {

		this.recommendationEngine = recommendationEngine;
	}

	/**
	 * Analyze any instrument.
	 */
	public RecommendationResponse analyze(Long instrumentToken, String tradingSymbol, String exchange, String interval,
			LocalDateTime from, LocalDateTime to) {

		RecommendationRequest request = RecommendationRequest.builder().instrument(tradingSymbol).build();

		return recommendationEngine.recommend(request);
	}

	/**
	 * Convenience method for BANKNIFTY.
	 */
	public RecommendationResponse analyzeBankNifty(Long instrumentToken, LocalDateTime from, LocalDateTime to) {

		return analyze(instrumentToken, "BANKNIFTY", "NFO", "5minute", from, to);
	}
}