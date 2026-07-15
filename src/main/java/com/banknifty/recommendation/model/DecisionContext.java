package com.banknifty.recommendation.model;

import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;
import com.banknifty.market.regime.MarketRegimeResult;
import lombok.Builder;

@Builder
public record DecisionContext(

		RecommendationRequest request,

		IndicatorSnapshot indicators,

		MarketContext marketContext,

		MarketRegimeResult regime

) {
}