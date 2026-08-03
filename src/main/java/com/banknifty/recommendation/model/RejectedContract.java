package com.banknifty.recommendation.model;

import com.banknifty.enums.OptionType;

/** Explains why a visible contract did not enter the final ranked universe. */
public record RejectedContract(String tradingSymbol, OptionType optionType, Integer strikePrice,
		String reason) {
}
