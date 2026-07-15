package com.banknifty.indicator.result;

import lombok.Builder;

@Builder
public record ADXResult(

		double adx,

		double plusDI,

		double minusDI,

		boolean trending,

		boolean bullish,

		boolean bearish

) {

	/**
	 * Strong trend present.
	 */
	public boolean strongTrend() {
		return trending;
	}

}