package com.banknifty.model;

import java.math.BigDecimal;

/**
 * Result of MACD calculation.
 */
public record MacdResult(

        BigDecimal macd,

        BigDecimal signal,

        BigDecimal histogram,

        boolean bullishCross,

        boolean bearishCross

) {
}