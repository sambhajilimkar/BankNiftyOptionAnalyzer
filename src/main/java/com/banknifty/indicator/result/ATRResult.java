package com.banknifty.indicator.result;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Average True Range analysis result.
 */
@Builder
public record ATRResult(

        BigDecimal atr,

        int period,

        boolean highVolatility,

        boolean lowVolatility,

        BigDecimal volatilityPercentage

) {
}
