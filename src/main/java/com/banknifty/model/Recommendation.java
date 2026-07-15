package com.banknifty.model;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record Recommendation(

		RecommendationAction action,

		OptionType optionType,

		String tradingSymbol,

		Integer strike,

		BigDecimal spotPrice,

		BigDecimal entry,

		BigDecimal stopLoss,

		BigDecimal target1,

		BigDecimal target2,

		Integer confidence,

		List<String> reasons

) {
}
