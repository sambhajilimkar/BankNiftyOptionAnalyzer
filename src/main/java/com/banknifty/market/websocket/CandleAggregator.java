package com.banknifty.market.websocket;

import com.banknifty.model.Candle;
import com.zerodhatech.models.Tick;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleAggregator {

	/**
	 * Active candles keyed by Instrument Token.
	 */
	private final Map<Long, Candle> activeCandles = new ConcurrentHashMap<>();

	/**
	 * Process one incoming Tick.
	 */
	public void onTick(Tick tick) {

		if (tick == null) {
			return;
		}

		Long instrumentToken = tick.getInstrumentToken();

		LocalDateTime tickTime;

		if (tick.getTickTimestamp() != null) {

			tickTime = tick.getTickTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		} else {

			tickTime = LocalDateTime.now();

		}

		BigDecimal ltp = BigDecimal.valueOf(tick.getLastTradedPrice());

		Candle previous = activeCandles.get(instrumentToken);

		Candle candle;

		if (previous == null) {

			candle = Candle.builder()

					.instrumentToken(instrumentToken)

					.tradingSymbol("")

					.exchange("")

					.interval("1minute")

					.tradeDate(LocalDate.from(tickTime))

					.dateTime(tickTime.withSecond(0).withNano(0))

					.open(ltp)

					.high(ltp)

					.low(ltp)

					.close(ltp)

					.volume((long) tick.getVolumeTradedToday())

					.openInterest((long) tick.getOi())

					.averagePrice(ltp)

					.vwap(BigDecimal.ZERO)

					.tickCount(1)

					.completed(false)

					.build();

		} else {

			candle = Candle.builder()

					.instrumentToken(previous.instrumentToken())

					.tradingSymbol(previous.tradingSymbol())

					.exchange(previous.exchange())

					.interval(previous.interval())

					.tradeDate(previous.tradeDate())

					.dateTime(previous.dateTime())

					.open(previous.open())

					.high(previous.high().max(ltp))

					.low(previous.low().min(ltp))

					.close(ltp)

					.volume((long) tick.getVolumeTradedToday())

					.openInterest((long) tick.getOi())

					.averagePrice(previous.averagePrice())

					.vwap(previous.vwap())

					.tickCount(previous.tickCount() + 1)

					.completed(false)

					.build();

		}

		activeCandles.put(instrumentToken, candle);

	}

	/**
	 * Returns current active candle.
	 */
	public Candle getActiveCandle(Long instrumentToken) {

		return activeCandles.get(instrumentToken);

	}

	/**
	 * Remove completed candle.
	 */
	public void remove(Long instrumentToken) {

		activeCandles.remove(instrumentToken);

	}

	/**
	 * Clear cache.
	 */
	public void clear() {

		activeCandles.clear();

	}

}