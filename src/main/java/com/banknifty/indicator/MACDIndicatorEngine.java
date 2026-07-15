package com.banknifty.indicator;

import com.banknifty.indicator.result.MACDResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MACDIndicatorEngine extends AbstractIndicator<MACDResult> {

	private static final int FAST = 12;
	private static final int SLOW = 26;
	private static final int SIGNAL = 9;

	public MACDIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public MACDResult calculate(List<Candle> candles) {

		validate(candles, SLOW + SIGNAL);

		BarSeries series = series(candles);

		ClosePriceIndicator close = new ClosePriceIndicator(series);

		MACDIndicator macd = new MACDIndicator(close, FAST, SLOW);

		EMAIndicator signal = new EMAIndicator(macd, SIGNAL);

		int last = lastIndex(series);

		BigDecimal macdValue = value(macd.getValue(last));
		BigDecimal signalValue = value(signal.getValue(last));

		BigDecimal previousMacd = value(macd.getValue(last - 1));

		BigDecimal previousSignal = value(signal.getValue(last - 1));

		BigDecimal histogram = macdValue.subtract(signalValue);

		boolean bullishCross = previousMacd.compareTo(previousSignal) <= 0 && macdValue.compareTo(signalValue) > 0;

		boolean bearishCross = previousMacd.compareTo(previousSignal) >= 0 && macdValue.compareTo(signalValue) < 0;

		boolean bullish = macdValue.compareTo(BigDecimal.ZERO) > 0;

		boolean bearish = macdValue.compareTo(BigDecimal.ZERO) < 0;

		return MACDResult.builder()

				.macd(macdValue)

				.signal(signalValue)

				.histogram(histogram)

				.previousMacd(previousMacd)

				.previousSignal(previousSignal)

				.bullishCross(bullishCross)

				.bearishCross(bearishCross)

				.bullish(bullish)

				.bearish(bearish)

				.build();

	}

}