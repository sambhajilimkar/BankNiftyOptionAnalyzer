package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.DecisionContext;
import com.banknifty.enums.RecommendationAction;

public interface DecisionEngine {

	RecommendationAction decide(DecisionContext context);

}