package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;

public interface RecommendationEngine {

	TradeRecommendation recommend(RecommendationRequest request);

}