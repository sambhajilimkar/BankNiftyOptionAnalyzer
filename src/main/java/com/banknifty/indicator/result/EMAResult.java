package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * EMA Indicator Result.
 */
@Builder
public record EMAResult(

		BigDecimal ema9,

		BigDecimal ema20,

		BigDecimal ema50,

		BigDecimal ema100,

		BigDecimal ema200,

		BigDecimal previousEma20,

		BigDecimal slope,

		boolean bullishCross,

		boolean bearishCross,

		boolean rising,

		boolean falling

) {

	/**
	 * Trend Direction
	 */
	public String trend() {

		if (bullishCross) {
			return "BULLISH";
		}

		if (bearishCross) {
			return "BEARISH";
		}

		if (rising) {
			return "UPTREND";
		}

		if (falling) {
			return "DOWNTREND";
		}

		return "SIDEWAYS";

	}

	public boolean bullishAlignment() {

		return ema20.compareTo(ema50) > 0 && ema50.compareTo(ema200) > 0;

	}

	public boolean bearishAlignment() {

		return ema20.compareTo(ema50) < 0 && ema50.compareTo(ema200) < 0;

	}

}