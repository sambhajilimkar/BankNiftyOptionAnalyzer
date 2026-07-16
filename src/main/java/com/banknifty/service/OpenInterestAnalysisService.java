package com.banknifty.service;

import com.banknifty.model.Candle;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Production Open Interest Analysis Service.
 *
 * Detects: - Long Build-up - Short Build-up - Long Unwinding - Short Covering
 */
@Service
public class OpenInterestAnalysisService {

	public OpenInterestResult analyze(List<Candle> candles) {

		validate(candles);

		Candle previous = candles.get(candles.size() - 2);

		Candle current = candles.get(candles.size() - 1);

		boolean priceUp = current.close().compareTo(previous.close()) > 0;

		boolean priceDown = current.close().compareTo(previous.close()) < 0;

		boolean oiUp = current.openInterest() > previous.openInterest();

		boolean oiDown = current.openInterest() < previous.openInterest();

		OpenInterestTrend trend;

		if (priceUp && oiUp) {

			trend = OpenInterestTrend.LONG_BUILDUP;

		} else if (priceDown && oiUp) {

			trend = OpenInterestTrend.SHORT_BUILDUP;

		} else if (priceUp) {

			trend = OpenInterestTrend.SHORT_COVERING;

		} else if (priceDown) {

			trend = OpenInterestTrend.LONG_UNWINDING;

		} else {

			trend = OpenInterestTrend.NEUTRAL;

		}

		return OpenInterestResult.builder()

				.previousOI(previous.openInterest())

				.currentOI(current.openInterest())

				.change(current.openInterest() - previous.openInterest())

				.priceUp(priceUp)

				.priceDown(priceDown)

				.oiIncreasing(oiUp)

				.oiDecreasing(oiDown)

				.trend(trend)

				.build();

	}

	public boolean isBullish(List<Candle> candles) {

		OpenInterestTrend trend = analyze(candles).trend();

		return trend == OpenInterestTrend.LONG_BUILDUP

				||

				trend == OpenInterestTrend.SHORT_COVERING;

	}

	public boolean isBearish(List<Candle> candles) {

		OpenInterestTrend trend = analyze(candles).trend();

		return trend == OpenInterestTrend.SHORT_BUILDUP

				||

				trend == OpenInterestTrend.LONG_UNWINDING;

	}

	private void validate(List<Candle> candles) {

		if (candles == null || candles.size() < 2) {

			throw new IllegalArgumentException("Minimum two candles are required.");

		}

	}

	@Builder
	public record OpenInterestResult(

			long previousOI,

			long currentOI,

			long change,

			boolean priceUp,

			boolean priceDown,

			boolean oiIncreasing,

			boolean oiDecreasing,

			OpenInterestTrend trend

	) {
	}

	public enum OpenInterestTrend {

		LONG_BUILDUP,

		SHORT_BUILDUP,

		LONG_UNWINDING,

		SHORT_COVERING,

		NEUTRAL

	}

}