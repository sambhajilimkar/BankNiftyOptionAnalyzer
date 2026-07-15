package com.banknifty.service;

import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.enums.RecommendationAction;

public interface TradingRuleEngine {

	RecommendationAction evaluate(IndicatorSnapshot snapshot);

}