package com.banknifty.service;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Performs Open Interest analysis on option candles.
 */
@Service
public class OpenInterestAnalysisService {

	/**
	 * Returns true when Open Interest is increasing.
	 */
	public boolean isOpenInterestIncreasing(List<Candle> candles) {

		validate(candles);

		long previous = candles.get(candles.size() - 2).openInterest();
		long current = candles.get(candles.size() - 1).openInterest();

		return current > previous;
	}

	/**
	 * Returns true when Open Interest is decreasing.
	 */
	public boolean isOpenInterestDecreasing(List<Candle> candles) {

		validate(candles);

		long previous = candles.get(candles.size() - 2).openInterest();
		long current = candles.get(candles.size() - 1).openInterest();

		return current < previous;
	}

	/**
	 * Returns change in Open Interest.
	 */
	public long openInterestChange(List<Candle> candles) {

		validate(candles);

		long previous = candles.get(candles.size() - 2).openInterest();
		long current = candles.get(candles.size() - 1).openInterest();

		return current - previous;
	}

	private void validate(List<Candle> candles) {
		if (candles == null || candles.size() < 2) {
			throw new IllegalArgumentException("Minimum two candles are required.");
		}
	}
}
