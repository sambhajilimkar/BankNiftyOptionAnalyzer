package com.banknifty.indicator;

import com.banknifty.model.Candle;

import java.util.List;

/**
 * Generic contract for every technical indicator.
 *
 * @param <T> Result type returned by the indicator.
 */
@FunctionalInterface
public interface IndicatorCalculator<T> {

	/**
	 * Calculates indicator using historical candles.
	 *
	 * @param candles historical candles
	 * @return calculated indicator
	 */
	T calculate(List<Candle> candles);

}