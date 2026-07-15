package com.banknifty.indicator;

import com.banknifty.indicator.result.EMAResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class EMAIndicatorEngine extends AbstractIndicator<EMAResult> {

	public EMAIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public EMAResult calculate(List<Candle> candles) {

		validate(candles, 200);

		BarSeries series = series(candles);

		ClosePriceIndicator close = new ClosePriceIndicator(series);

		org.ta4j.core.indicators.averages.EMAIndicator ema9 = new org.ta4j.core.indicators.averages.EMAIndicator(close,
				9);

		org.ta4j.core.indicators.averages.EMAIndicator ema20 = new org.ta4j.core.indicators.averages.EMAIndicator(close,
				20);

		org.ta4j.core.indicators.averages.EMAIndicator ema50 = new org.ta4j.core.indicators.averages.EMAIndicator(close,
				50);

		org.ta4j.core.indicators.averages.EMAIndicator ema100 = new org.ta4j.core.indicators.averages.EMAIndicator(
				close, 100);

		org.ta4j.core.indicators.averages.EMAIndicator ema200 = new org.ta4j.core.indicators.averages.EMAIndicator(
				close, 200);

		int last = series.getEndIndex();

		BigDecimal e9 = value(ema9, last);
		BigDecimal e20 = value(ema20, last);
		BigDecimal e50 = value(ema50, last);
		BigDecimal e100 = value(ema100, last);
		BigDecimal e200 = value(ema200, last);

		BigDecimal previous20 = value(ema20, last - 1);

		BigDecimal slope = e20.subtract(previous20);

		boolean rising = slope.compareTo(BigDecimal.ZERO) > 0;

		boolean falling = slope.compareTo(BigDecimal.ZERO) < 0;

		boolean bullishCross = e20.compareTo(e50) > 0 && previous20.compareTo(value(ema50, last - 1)) <= 0;

		boolean bearishCross = e20.compareTo(e50) < 0 && previous20.compareTo(value(ema50, last - 1)) >= 0;

		return EMAResult.builder()

				.ema9(e9)

				.ema20(e20)

				.ema50(e50)

				.ema100(e100)

				.ema200(e200)

				.previousEma20(previous20)

				.slope(slope.setScale(2, RoundingMode.HALF_UP))

				.bullishCross(bullishCross)

				.bearishCross(bearishCross)

				.rising(rising)

				.falling(falling)

				.build();

	}

	private BigDecimal value(org.ta4j.core.indicators.averages.EMAIndicator indicator, int index) {

		return BigDecimal.valueOf(indicator.getValue(index).doubleValue()).setScale(2, RoundingMode.HALF_UP);

	}

}