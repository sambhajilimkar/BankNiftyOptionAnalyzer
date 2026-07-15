package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * SuperTrend analysis result.
 */
@Builder
public record SuperTrendResult(

        BigDecimal superTrend,

        BigDecimal upperBand,

        BigDecimal lowerBand,

        boolean bullish,

        boolean bearish,

        boolean trendChanged,

        int atrPeriod,

        BigDecimal multiplier

) {
}
