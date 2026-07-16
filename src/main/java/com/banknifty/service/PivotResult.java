package com.banknifty.service;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PivotResult(

		BigDecimal pivot,

		BigDecimal bc,

		BigDecimal tc,

		BigDecimal r1,

		BigDecimal r2,

		BigDecimal r3,

		BigDecimal s1,

		BigDecimal s2,

		BigDecimal s3,

		BigDecimal cprWidth,

		boolean narrowCPR,

		boolean wideCPR,

		boolean priceAbovePivot,

		boolean priceBelowPivot,

		boolean bullish,

		boolean bearish

) {
}