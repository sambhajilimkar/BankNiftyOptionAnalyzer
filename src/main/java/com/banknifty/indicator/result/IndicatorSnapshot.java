package com.banknifty.indicator.result;

import lombok.Builder;

/**
 * Immutable snapshot of all calculated technical indicators for a completed
 * candle.
 *
 * NOTE: This class is intentionally a DTO only. No trading/business logic
 * should be added here.
 */
@Builder
public record IndicatorSnapshot(

		EMAResult ema,

		RSIResult rsi,

		MACDResult macd,

		ATRResult atr,

		ADXResult adx,

		VWAPResult vwap,

		SuperTrendResult superTrend

) {
}