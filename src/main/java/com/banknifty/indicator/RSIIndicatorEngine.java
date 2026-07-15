package com.banknifty.indicator;

import com.banknifty.indicator.result.RSIResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RSIIndicatorEngine extends AbstractIndicator<RSIResult> {

	private static final int DEFAULT_PERIOD = 14;

	public RSIIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public RSIResult calculate(List<Candle> candles) {

		validate(candles, DEFAULT_PERIOD + 1);

		BarSeries series = series(candles);

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		RSIIndicator rsi = new RSIIndicator(closePrice, DEFAULT_PERIOD);

		int last = lastIndex(series);

		BigDecimal current = value(rsi.getValue(last));

		BigDecimal previous = value(rsi.getValue(last - 1));

		boolean rising = rising(current, previous);

		boolean falling = falling(current, previous);

		boolean overBought = current.compareTo(BigDecimal.valueOf(70)) >= 0;

		boolean overSold = current.compareTo(BigDecimal.valueOf(30)) <= 0;

		boolean bullish = current.compareTo(BigDecimal.valueOf(50)) > 0;

		boolean bearish = current.compareTo(BigDecimal.valueOf(50)) < 0;

		return RSIResult.builder()

				.rsi(current)

				.previousRsi(previous)

				.overBought(overBought)

				.overSold(overSold)

				.bullish(bullish)

				.bearish(bearish)

				.rising(rising)

				.falling(falling)

				.build();

	}

}