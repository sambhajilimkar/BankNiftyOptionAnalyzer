package com.banknifty.recommendation.model;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Final recommendation produced by Recommendation Engine.
 */
@Builder
public record TradeRecommendation(

		RecommendationAction action,

		String instrument,

		LocalDate expiryDate,

		String expiryLabel,

		OptionType optionType,

		Integer strikePrice,

		BigDecimal spotPrice,

		BigDecimal optionPrice,

		BigDecimal entryMin,

		BigDecimal entryMax,

		BigDecimal stopLoss,

		BigDecimal target1,

		BigDecimal target2,

		BigDecimal target3,

		Integer confidence,

		RiskLevel risk,

		Integer quantity,

		String holdingTime,

		List<String> reasons,

		List<String> rejectedReasons,

		InstitutionalAnalysis institutionalAnalysis,

		Integer technicalConfidence

) {
}
