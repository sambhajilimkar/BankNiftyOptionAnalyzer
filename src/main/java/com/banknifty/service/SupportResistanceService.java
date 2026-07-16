package com.banknifty.service;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SupportResistanceService {

	private static final BigDecimal NEAR_PERCENT = BigDecimal.valueOf(0.003); // 0.30 %

	public SupportResistanceResult calculate(List<Candle> candles) {

		validate(candles);

		BigDecimal high = candles.stream().map(Candle::high).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

		BigDecimal low = candles.stream().map(Candle::low).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

		BigDecimal close = candles.get(candles.size() - 1).close();

		BigDecimal pivot = high.add(low).add(close).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

		BigDecimal r1 = pivot.multiply(BigDecimal.valueOf(2)).subtract(low);

		BigDecimal s1 = pivot.multiply(BigDecimal.valueOf(2)).subtract(high);

		BigDecimal range = high.subtract(low);

		BigDecimal r2 = pivot.add(range);

		BigDecimal s2 = pivot.subtract(range);

		BigDecimal r3 = high.add(pivot.subtract(low));

		BigDecimal s3 = low.subtract(high.subtract(pivot));

		BigDecimal nearestSupport = nearestSupport(close, s1, s2, s3);

		BigDecimal nearestResistance = nearestResistance(close, r1, r2, r3);

		BigDecimal supportDistance = close.subtract(nearestSupport).abs();

		BigDecimal resistanceDistance = nearestResistance.subtract(close).abs();

		BigDecimal threshold = close.multiply(NEAR_PERCENT);

		boolean nearSupport = supportDistance.compareTo(threshold) <= 0;

		boolean nearResistance = resistanceDistance.compareTo(threshold) <= 0;

		boolean breakout = close.compareTo(nearestResistance) > 0;

		boolean breakdown = close.compareTo(nearestSupport) < 0;

		return SupportResistanceResult.builder()

				.support1(scale(s1))

				.support2(scale(s2))

				.support3(scale(s3))

				.resistance1(scale(r1))

				.resistance2(scale(r2))

				.resistance3(scale(r3))

				.nearestSupport(scale(nearestSupport))

				.nearestResistance(scale(nearestResistance))

				.supportDistance(scale(supportDistance))

				.resistanceDistance(scale(resistanceDistance))

				.nearSupport(nearSupport)

				.nearResistance(nearResistance)

				.breakout(breakout)

				.breakdown(breakdown)

				.build();

	}

	private BigDecimal nearestSupport(BigDecimal price, BigDecimal s1, BigDecimal s2, BigDecimal s3) {

		BigDecimal support = s1;

		if (s2.compareTo(price) <= 0 && s2.compareTo(support) > 0) {
			support = s2;
		}

		if (s3.compareTo(price) <= 0 && s3.compareTo(support) > 0) {
			support = s3;
		}

		return support;
	}

	private BigDecimal nearestResistance(BigDecimal price, BigDecimal r1, BigDecimal r2, BigDecimal r3) {

		if (r1.compareTo(price) >= 0) {
			return r1;
		}

		if (r2.compareTo(price) >= 0) {
			return r2;
		}

		return r3;
	}

	private BigDecimal scale(BigDecimal value) {

		return value.setScale(2, RoundingMode.HALF_UP);

	}

	private void validate(List<Candle> candles) {

		if (candles == null || candles.size() < 20) {

			throw new IllegalArgumentException("Minimum 20 candles required.");

		}

	}

}