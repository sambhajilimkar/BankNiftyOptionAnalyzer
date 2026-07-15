package com.banknifty.indicator;

import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import lombok.RequiredArgsConstructor;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Base class for all technical indicators.
 *
 * Provides: - Candle validation - TA4J BarSeries creation - Common helper
 * methods
 *
 * @param <T> Indicator result type
 */
@RequiredArgsConstructor
public abstract class AbstractIndicator<T> implements IndicatorCalculator<T> {

	/**
	 * Shared BarSeries builder.
	 */
	protected final BarSeriesBuilder barSeriesBuilder;

	/**
	 * Build TA4J BarSeries.
	 */
	protected BarSeries series(List<Candle> candles) {

		validate(candles);

		return barSeriesBuilder.build(candles);

	}

	/**
	 * Validate candles.
	 */
	protected void validate(List<Candle> candles) {

		if (candles == null || candles.isEmpty()) {

			throw new IllegalArgumentException("Historical candles cannot be null or empty.");

		}

	}

	/**
	 * Validate minimum candles.
	 */
	protected void validate(List<Candle> candles, int minimumCandles) {

		validate(candles);

		if (candles.size() < minimumCandles) {

			throw new IllegalArgumentException("Minimum " + minimumCandles + " candles required.");

		}

	}

	/**
	 * Latest candle.
	 */
	protected Candle latest(List<Candle> candles) {

		validate(candles);

		return candles.get(candles.size() - 1);

	}

	/**
	 * Previous candle.
	 */
	protected Candle previous(List<Candle> candles) {

		validate(candles, 2);

		return candles.get(candles.size() - 2);

	}

	/**
	 * Last index.
	 */
	protected int lastIndex(BarSeries series) {

		return series.getEndIndex();

	}

	/**
	 * Convert TA4J Num to BigDecimal.
	 */
	protected BigDecimal value(Num num) {

		return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);

	}

	/**
	 * Compare two BigDecimals.
	 */
	protected boolean greater(BigDecimal first, BigDecimal second) {

		return first.compareTo(second) > 0;

	}

	/**
	 * Compare two BigDecimals.
	 */
	protected boolean less(BigDecimal first, BigDecimal second) {

		return first.compareTo(second) < 0;

	}

	/**
	 * Positive slope?
	 */
	protected boolean rising(BigDecimal current, BigDecimal previous) {

		return current.compareTo(previous) > 0;

	}

	/**
	 * Negative slope?
	 */
	protected boolean falling(BigDecimal current, BigDecimal previous) {

		return current.compareTo(previous) < 0;

	}

}