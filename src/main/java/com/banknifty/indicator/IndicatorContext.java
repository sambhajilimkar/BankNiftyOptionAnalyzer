package com.banknifty.indicator;

import com.banknifty.model.Candle;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared Indicator Context.
 *
 * All indicators receive this object instead of repeatedly extracting OHLCV
 * values.
 */
public record IndicatorContext(

		List<Candle> candles,

		BarSeries series,

		List<BigDecimal> opens,

		List<BigDecimal> highs,

		List<BigDecimal> lows,

		List<BigDecimal> closes,

		List<Long> volumes

) {

	/**
	 * Creates context.
	 */
	public static IndicatorContext from(List<Candle> candles, BarSeries series) {

		return new IndicatorContext(

				candles,

				series,

				candles.stream().map(Candle::open).collect(Collectors.toList()),

				candles.stream().map(Candle::high).collect(Collectors.toList()),

				candles.stream().map(Candle::low).collect(Collectors.toList()),

				candles.stream().map(Candle::close).collect(Collectors.toList()),

				candles.stream().map(Candle::volume).collect(Collectors.toList()));

	}

	/**
	 * Total candles.
	 */
	public int size() {
		return candles.size();
	}

	/**
	 * Enough candles?
	 */
	public boolean hasMinimumBars(int minimum) {
		return candles.size() >= minimum;
	}

	/**
	 * Latest Candle.
	 */
	public Candle latest() {
		return candles.get(candles.size() - 1);
	}

	/**
	 * Previous Candle.
	 */
	public Candle previous() {

		if (candles.size() < 2) {
			return null;
		}

		return candles.get(candles.size() - 2);

	}

	/**
	 * Last Close.
	 */
	public BigDecimal lastClose() {
		return closes.get(closes.size() - 1);
	}

	/**
	 * Previous Close.
	 */
	public BigDecimal previousClose() {

		if (closes.size() < 2) {
			return BigDecimal.ZERO;
		}

		return closes.get(closes.size() - 2);

	}

	/**
	 * Last High.
	 */
	public BigDecimal lastHigh() {
		return highs.get(highs.size() - 1);
	}

	/**
	 * Last Low.
	 */
	public BigDecimal lastLow() {
		return lows.get(lows.size() - 1);
	}

	/**
	 * Last Volume.
	 */
	public long lastVolume() {
		return volumes.get(volumes.size() - 1);
	}

	/**
	 * Average Volume.
	 */
	public long averageVolume() {

		return volumes.stream()

				.mapToLong(Long::longValue)

				.sum() / Math.max(1, volumes.size());

	}

	/**
	 * Candle body size.
	 */
	public BigDecimal bodySize() {

		return latest()

				.close()

				.subtract(latest().open())

				.abs();

	}

	/**
	 * Candle range.
	 */
	public BigDecimal candleRange() {

		return latest()

				.high()

				.subtract(latest().low());

	}

	/**
	 * Bullish candle?
	 */
	public boolean bullish() {

		return latest()

				.close()

				.compareTo(latest().open()) > 0;

	}

	/**
	 * Bearish candle?
	 */
	public boolean bearish() {

		return latest()

				.close()

				.compareTo(latest().open()) < 0;

	}

}