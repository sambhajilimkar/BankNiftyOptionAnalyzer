package com.banknifty.recommendation.model;

import com.banknifty.enums.OptionType;

/** One planned portfolio allocation; cash remains explicit rather than implied. */
public record AllocationLeg(String tradingSymbol, OptionType optionType, Integer strikePrice,
		int allocationPercent, String rationale) {
}
