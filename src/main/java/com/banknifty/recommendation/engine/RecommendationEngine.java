package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.model.RecommendationResponseV2;

public interface RecommendationEngine {

	TradeRecommendation recommend(RecommendationRequest request);

	/**
	 * Full decision packet for the dashboard, API consumers and backtesting.
	 * The legacy {@link #recommend(RecommendationRequest)} method remains stable
	 * for clients that only need the selected trade.
	 */
	RecommendationResponseV2 recommendV2(RecommendationRequest request);

}
