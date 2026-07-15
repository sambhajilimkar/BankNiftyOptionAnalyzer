package com.banknifty.recommendation.model;

import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import lombok.Builder;

@Builder
public record RecommendationRequest(

		String instrument,

		ExpiryType expiryType,

		TradingStyle tradingStyle,

		RiskProfile riskProfile,

		Double capital

) {
}