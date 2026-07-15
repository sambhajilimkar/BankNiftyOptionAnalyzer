package com.banknifty.indicator;

import com.banknifty.indicator.result.ATRResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * ATR Indicator using TA4J 0.18.
 */
@Component
public class ATRIndicatorEngine extends AbstractIndicator<ATRResult> {

	private static final int DEFAULT_PERIOD = 14;

	public ATRIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public ATRResult calculate(List<Candle> candles) {

		return calculate(candles, DEFAULT_PERIOD);

	}

	public ATRResult calculate(List<Candle> candles, int period) {

		validate(candles, period + 1);

		BarSeries series = series(candles);

		org.ta4j.core.indicators.ATRIndicator atrIndicator = new org.ta4j.core.indicators.ATRIndicator(series, period);

		BigDecimal atr = ((BigDecimal) atrIndicator.getValue(series.getEndIndex()).getDelegate()).setScale(2,
				RoundingMode.HALF_UP);

		BigDecimal close = candles.get(candles.size() - 1).close();

		BigDecimal volatilityPercentage = atr.divide(close, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		boolean highVolatility = volatilityPercentage.compareTo(BigDecimal.valueOf(1.5)) > 0;

		boolean lowVolatility = !highVolatility;

		return ATRResult.builder()

				.atr(atr)

				.period(period)

				.highVolatility(highVolatility)

				.lowVolatility(lowVolatility)

				.volatilityPercentage(volatilityPercentage)

				.build();

	}

	public boolean isHighVolatility(List<Candle> candles) {

		return calculate(candles).highVolatility();

	}

	public boolean isLowVolatility(List<Candle> candles) {

		return calculate(candles).lowVolatility();

	}

}