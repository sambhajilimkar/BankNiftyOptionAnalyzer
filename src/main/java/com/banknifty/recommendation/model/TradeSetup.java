package com.banknifty.recommendation.model;

import com.banknifty.enums.OptionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One complete trade setup before final recommendation.
 */
@Builder
public record TradeSetup(

		String instrument,

		LocalDate expiry,

		OptionType optionType,

		Integer strike,

		BigDecimal spotPrice,

		BigDecimal optionPrice,

		BigDecimal entry,

		BigDecimal stopLoss,

		BigDecimal target1,

		BigDecimal target2,

		BigDecimal target3,

		BigDecimal riskReward,

		Integer setupScore,

		List<String> reasons

) {
}