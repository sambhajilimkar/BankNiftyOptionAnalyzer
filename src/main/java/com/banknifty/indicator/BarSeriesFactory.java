package com.banknifty.indicator;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class BarSeriesFactory {

	/**
	 * Builds TA4J BarSeries from immutable Candle records.
	 */
	public BarSeries build(List<Candle> candles) {

		BarSeries series = new BaseBarSeriesBuilder()

				.withName("BANKNIFTY")

				.build();

		for (Candle candle : candles) {

			ZonedDateTime endTime = candle.dateTime().atZone(ZoneId.systemDefault());

			BaseBar bar = new org.ta4j.core.BaseBar(

					Duration.ofMinutes(resolveInterval(candle.interval())),

					endTime.toInstant(),

					DecimalNum.valueOf(candle.open()),

					DecimalNum.valueOf(candle.high()),

					DecimalNum.valueOf(candle.low()),

					DecimalNum.valueOf(candle.close()),

					DecimalNum.valueOf(candle.volume()),

					DecimalNum.valueOf(0),

					0L

			);
			series.addBar(bar);

		}

		return series;

	}

	private long resolveInterval(String interval) {

		if (interval == null)
			return 5;

		return switch (interval.toUpperCase()) {

		case "1MINUTE", "1MIN", "1" -> 1;

		case "3MINUTE", "3MIN" -> 3;

		case "5MINUTE", "5MIN" -> 5;

		case "10MINUTE", "10MIN" -> 10;

		case "15MINUTE", "15MIN" -> 15;

		case "30MINUTE", "30MIN" -> 30;

		case "60MINUTE", "60MIN", "1HOUR" -> 60;

		default -> 5;

		};

	}

}