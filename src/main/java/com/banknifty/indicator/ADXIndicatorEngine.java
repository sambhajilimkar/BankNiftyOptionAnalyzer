package com.banknifty.indicator;

import com.banknifty.indicator.result.ADXResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

import java.util.List;

/**
 * Production ADX indicator using TA4J 0.18.
 */
@Component
public class ADXIndicatorEngine extends AbstractIndicator<ADXResult> {

	private static final int PERIOD = 14;
	private static final double TREND_THRESHOLD = 25.0;

	public ADXIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public ADXResult calculate(List<Candle> candles) {

		validate(candles, PERIOD + 1);

		BarSeries series = series(candles);

		org.ta4j.core.indicators.adx.ADXIndicator adx = new org.ta4j.core.indicators.adx.ADXIndicator(series, PERIOD);
		PlusDIIndicator plusDI = new PlusDIIndicator(series, PERIOD);
		MinusDIIndicator minusDI = new MinusDIIndicator(series, PERIOD);

		int index = series.getEndIndex();

		double adxValue = adx.getValue(index).doubleValue();
		double plus = plusDI.getValue(index).doubleValue();
		double minus = minusDI.getValue(index).doubleValue();

		boolean trending = adxValue >= TREND_THRESHOLD;
		boolean bullish = trending && plus > minus;
		boolean bearish = trending && minus > plus;

		return ADXResult.builder().adx(adxValue).plusDI(plus).minusDI(minus).trending(trending).bullish(bullish)
				.bearish(bearish).build();
	}

	public boolean isTrending(List<Candle> candles) {
		return calculate(candles).trending();
	}

	public boolean isBullishTrend(List<Candle> candles) {
		return calculate(candles).bullish();
	}

	public boolean isBearishTrend(List<Candle> candles) {
		return calculate(candles).bearish();
	}
}
