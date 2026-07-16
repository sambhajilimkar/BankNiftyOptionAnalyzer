package com.banknifty.service;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SupportResistanceResult(

		BigDecimal support1,

		BigDecimal support2,

		BigDecimal support3,

		BigDecimal resistance1,

		BigDecimal resistance2,

		BigDecimal resistance3,

		BigDecimal nearestSupport,

		BigDecimal nearestResistance,

		BigDecimal supportDistance,

		BigDecimal resistanceDistance,

		boolean nearSupport,

		boolean nearResistance,

		boolean breakout,

		boolean breakdown

) {
}