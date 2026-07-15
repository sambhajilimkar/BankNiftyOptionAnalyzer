package com.banknifty.market.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleScheduler {

	private static final LocalTime MARKET_OPEN = LocalTime.of(9, 15);
	private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

	private final ActiveCandleManager activeCandleManager;

	private final TickAggregator tickAggregator;

	/**
	 * Runs every minute. Closes all active candles.
	 */
	@Scheduled(cron = "0 * * * * *")
	public void closeCandles() {

		LocalDateTime now = LocalDateTime.now();

		if (!isMarketOpen(now)) {
			return;
		}

		if (activeCandleManager.getAll().isEmpty()) {
			return;
		}

		log.debug("Closing {} active candles.", activeCandleManager.getAll().size());

		activeCandleManager.getAll().keySet().forEach(this::closeInstrument);

	}

	/**
	 * Close one instrument safely.
	 */
	private void closeInstrument(Long instrumentToken) {

		try {

			tickAggregator.close(instrumentToken);

		} catch (Exception ex) {

			log.error("Unable to close candle for {}", instrumentToken, ex);

		}

	}

	/**
	 * NSE Market timing.
	 */
	private boolean isMarketOpen(LocalDateTime dateTime) {

		DayOfWeek day = dateTime.getDayOfWeek();

		if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {

			return false;

		}

		LocalTime time = dateTime.toLocalTime();

		return !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);

	}

}