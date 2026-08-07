package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.RecommendationResponse;

public interface RecommendationEngine {

	RecommendationResponse recommend(RecommendationRequest request);

}