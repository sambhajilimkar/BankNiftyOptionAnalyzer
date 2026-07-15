package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VWAPResult(

		BigDecimal vwap,

		BigDecimal currentPrice,

		BigDecimal distance,

		boolean priceAboveVWAP,

		boolean breakout,

		boolean pullback

) {

	/**
	 * Price is trading above VWAP.
	 */
	public boolean aboveVWAP() {
		return priceAboveVWAP;
	}

	/**
	 * Price is trading below VWAP.
	 */
	public boolean belowVWAP() {
		return !priceAboveVWAP;
	}

}