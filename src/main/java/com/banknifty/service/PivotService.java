package com.banknifty.service;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PivotService {

	public PivotResult calculate(List<Candle> candles) {

		validate(candles);

		Candle previous = candles.get(candles.size() - 2);

		Candle current = candles.get(candles.size() - 1);

		BigDecimal high = previous.high();

		BigDecimal low = previous.low();

		BigDecimal close = previous.close();

		BigDecimal currentPrice = current.close();

		/*
		 * Classic Pivot
		 */
		BigDecimal pivot = high.add(low).add(close).divide(BigDecimal.valueOf(3), 8, RoundingMode.HALF_UP);

		/*
		 * CPR
		 */
		BigDecimal bc = high.add(low).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);

		BigDecimal tc = pivot.multiply(BigDecimal.valueOf(2)).subtract(bc);

		/*
		 * Resistance
		 */
		BigDecimal r1 = pivot.multiply(BigDecimal.valueOf(2)).subtract(low);

		BigDecimal r2 = pivot.add(high.subtract(low));

		BigDecimal r3 = high.add(pivot.subtract(low));

		/*
		 * Support
		 */
		BigDecimal s1 = pivot.multiply(BigDecimal.valueOf(2)).subtract(high);

		BigDecimal s2 = pivot.subtract(high.subtract(low));

		BigDecimal s3 = low.subtract(high.subtract(pivot));

		/*
		 * CPR Width
		 */
		BigDecimal cprWidth = tc.subtract(bc).abs();

		BigDecimal widthPercent = cprWidth.divide(currentPrice, 8, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		boolean narrowCPR = widthPercent.compareTo(BigDecimal.valueOf(0.40)) <= 0;

		boolean wideCPR = widthPercent.compareTo(BigDecimal.ONE) >= 0;

		boolean abovePivot = currentPrice.compareTo(pivot) > 0;

		boolean belowPivot = currentPrice.compareTo(pivot) < 0;

		boolean bullish = abovePivot && currentPrice.compareTo(r1) >= 0;

		boolean bearish = belowPivot && currentPrice.compareTo(s1) <= 0;

		return PivotResult.builder()

				.pivot(scale(pivot))

				.bc(scale(bc))

				.tc(scale(tc))

				.r1(scale(r1))

				.r2(scale(r2))

				.r3(scale(r3))

				.s1(scale(s1))

				.s2(scale(s2))

				.s3(scale(s3))

				.cprWidth(scale(cprWidth))

				.narrowCPR(narrowCPR)

				.wideCPR(wideCPR)

				.priceAbovePivot(abovePivot)

				.priceBelowPivot(belowPivot)

				.bullish(bullish)

				.bearish(bearish)

				.build();

	}

	private BigDecimal scale(BigDecimal value) {

		return value.setScale(2, RoundingMode.HALF_UP);

	}

	private void validate(List<Candle> candles) {

		if (candles == null || candles.size() < 2) {

			throw new IllegalArgumentException("Minimum two candles are required.");

		}

	}

}