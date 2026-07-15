package com.banknifty.market.realtime;

import com.banknifty.model.Candle;
import com.zerodhatech.models.Tick;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class CandleBuilder {

	/**
	 * Creates first candle from incoming tick.
	 */
	public Candle newCandle(Tick tick, String tradingSymbol, String exchange, String interval) {

		BigDecimal price = BigDecimal.valueOf(tick.getLastTradedPrice());

		LocalDateTime tickTime = LocalDateTime.ofInstant(tick.getTickTimestamp().toInstant(), ZoneId.systemDefault());

		return Candle.builder()

				.instrumentToken(tick.getInstrumentToken())

				.tradingSymbol(tradingSymbol)

				.exchange(exchange)

				.interval(interval)

				.tradeDate(tickTime.toLocalDate())

				.dateTime(roundTime(tickTime, interval))

				.open(price)

				.high(price)

				.low(price)

				.close(price)

				.volume(tick.getVolumeTradedToday())

				.openInterest(0L)

				.averagePrice(price)

				.vwap(price)

				.tickCount(1)

				.completed(false)

				.build();

	}

	/**
	 * Updates active candle.
	 */
	public Candle update(Candle candle, Tick tick) {

		BigDecimal price = BigDecimal.valueOf(tick.getLastTradedPrice());

		int tickCount = candle.tickCount() + 1;

		BigDecimal averagePrice = candle.averagePrice()

				.multiply(BigDecimal.valueOf(candle.tickCount()))

				.add(price)

				.divide(BigDecimal.valueOf(tickCount), 6, RoundingMode.HALF_UP);

		/*
		 * Placeholder VWAP. We'll replace this with true VWAP after Volume Engine.
		 */
		BigDecimal vwap = averagePrice;

		return candle.toBuilder()

				.high(candle.high().max(price))

				.low(candle.low().min(price))

				.close(price)

				.volume(tick.getVolumeTradedToday())

				.averagePrice(averagePrice)

				.vwap(vwap)

				.tickCount(tickCount)

				.build();

	}

	/**
	 * Complete candle.
	 */
	public Candle complete(Candle candle) {

		return candle.toBuilder()

				.completed(true)

				.build();

	}

	/**
	 * Round timestamp according to interval.
	 */
	private LocalDateTime roundTime(LocalDateTime time, String interval) {

		return switch (interval.toLowerCase()) {

		case "3minute" -> time.withMinute((time.getMinute() / 3) * 3).withSecond(0).withNano(0);

		case "5minute" -> time.withMinute((time.getMinute() / 5) * 5).withSecond(0).withNano(0);

		case "15minute" -> time.withMinute((time.getMinute() / 15) * 15).withSecond(0).withNano(0);

		case "30minute" -> time.withMinute((time.getMinute() / 30) * 30).withSecond(0).withNano(0);

		case "60minute" -> time.withMinute(0).withSecond(0).withNano(0);

		default -> time.withSecond(0).withNano(0);

		};

	}

}