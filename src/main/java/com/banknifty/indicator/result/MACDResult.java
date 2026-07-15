package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * MACD Indicator Result.
 */
@Builder
public record MACDResult(

		BigDecimal macd,

		BigDecimal signal,

		BigDecimal histogram,

		BigDecimal previousMacd,

		BigDecimal previousSignal,

		boolean bullishCross,

		boolean bearishCross,

		boolean bullish,

		boolean bearish

) {

	/**
	 * Trend.
	 */
	public String trend() {

		if (bullishCross) {
			return "BULLISH_CROSS";
		}

		if (bearishCross) {
			return "BEARISH_CROSS";
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