package com.banknifty.model;

import java.math.BigDecimal;

public record IndicatorResult(

        // EMA
        BigDecimal ema20,
        BigDecimal ema50,
        BigDecimal ema200,

        // RSI
        BigDecimal rsi,

        // MACD
        BigDecimal macd,
        BigDecimal macdSignal,
        BigDecimal macdHistogram,

        // VWAP
        BigDecimal vwap,

        // ADX
        BigDecimal adx,

        // ATR
        BigDecimal atr,

        // Bollinger Bands
        BigDecimal upperBand,
        BigDecimal middleBand,
        BigDecimal lowerBand,

        // SuperTrend
        BigDecimal superTrend,

        // Volume
        Long currentVolume,
        Long averageVolume,

        // Trend Information
        boolean emaBullish,
        boolean macdBullish,
        boolean aboveVWAP,
        boolean strongTrend,
        boolean volumeSpike

) {
}