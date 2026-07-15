package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * RSI Indicator Result.
 */
@Builder
public record RSIResult(

		BigDecimal rsi,

		BigDecimal previousRsi,

		boolean overBought,

		boolean overSold,

		boolean bullish,

		boolean bearish,

		boolean rising,

		boolean falling

) {

	/**
	 * RSI Trend.
	 */
	public String trend() {

		if (overBought) {
			return "OVERBOUGHT";
		}

		if (overSold) {
			return "OVERSOLD";
		}

		if (bullish) {
			return "BULLISH";
		}

		if (bearish) {
			return "BEARISH";
		}

		return "NEUTRAL";

	}

}