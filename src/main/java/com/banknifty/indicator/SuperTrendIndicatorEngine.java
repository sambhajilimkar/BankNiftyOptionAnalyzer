package com.banknifty.indicator;

import com.banknifty.indicator.result.ATRResult;
import com.banknifty.indicator.result.SuperTrendResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * SuperTrend Indicator.
 *
 * Compatible with: - Java 21 - Spring Boot 3.5 - ATRResult - Immutable Candle
 * record
 */
@Component
public class SuperTrendIndicatorEngine extends AbstractIndicator<SuperTrendResult> {

	private static final int DEFAULT_PERIOD = 10;

	private static final BigDecimal DEFAULT_MULTIPLIER = BigDecimal.valueOf(3);

	private final ATRIndicatorEngine atrIndicator;

	public SuperTrendIndicatorEngine(BarSeriesBuilder barSeriesBuilder, ATRIndicatorEngine atrIndicator) {

		super(barSeriesBuilder);
		this.atrIndicator = atrIndicator;

	}

	@Override
	public SuperTrendResult calculate(List<Candle> candles) {

		return calculate(candles, DEFAULT_PERIOD, DEFAULT_MULTIPLIER);

	}

	public SuperTrendResult calculate(List<Candle> candles, int period, BigDecimal multiplier) {

		validate(candles, period + 1);

		Candle last = candles.get(candles.size() - 1);

		ATRResult atrResult = atrIndicator.calculate(candles, period);

		BigDecimal atr = atrResult.atr();

		BigDecimal hl2 = last.high().add(last.low()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);

		BigDecimal upperBand = hl2.add(atr.multiply(multiplier));

		BigDecimal lowerBand = hl2.subtract(atr.multiply(multiplier));

		boolean bullish = last.close().compareTo(upperBand) > 0;

		boolean bearish = last.close().compareTo(lowerBand) < 0;

		BigDecimal superTrend = bullish ? lowerBand : upperBand;

		boolean trendChanged = bullish || bearish;

		return SuperTrendResult.builder()

				.superTrend(superTrend)

				.upperBand(upperBand)

				.lowerBand(lowerBand)

				.bullish(bullish)

				.bearish(bearish)

				.trendChanged(trendChanged)

				.atrPeriod(period)

				.multiplier(multiplier)

				.build();

	}

	public boolean isBullish(List<Candle> candles) {

		return calculate(candles).bullish();

	}

	public boolean isBearish(List<Candle> candles) {

		return calculate(candles).bearish();

	}

}