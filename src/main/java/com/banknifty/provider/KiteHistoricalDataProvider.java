package com.banknifty.provider;

import com.banknifty.exception.GatewayException;
import com.banknifty.gateway.GatewayHealthService;
import com.banknifty.model.Candle;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class KiteHistoricalDataProvider implements HistoricalDataProvider {

	/**
	 * Zerodha format:
	 *
	 * 2026-07-06T00:00:00+0530
	 */
	private static final DateTimeFormatter KITE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	private final KiteConnect kiteConnect;

	private final GatewayHealthService healthService;

	public KiteHistoricalDataProvider(KiteConnect kiteConnect, GatewayHealthService healthService) {

		this.kiteConnect = kiteConnect;
		this.healthService = healthService;
	}

	@Override
	public List<Candle> fetchHistoricalData(Long instrumentToken, String tradingSymbol, String exchange,
			String interval, LocalDateTime from, LocalDateTime to, boolean continuous, boolean openInterest) {

		try {

			log.info("Fetching historical candles [{}] {} -> {}", interval, from, to);

			HistoricalData historicalData = null;
			try {
				historicalData = kiteConnect.getHistoricalData(

						Date.from(from.atZone(ZoneId.systemDefault()).toInstant()),

						Date.from(to.atZone(ZoneId.systemDefault()).toInstant()),

						instrumentToken.toString(),

						interval,

						continuous,

						openInterest);
			} catch (KiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			healthService.markSuccess();

			if (historicalData == null || historicalData.dataArrayList == null) {

				return List.of();

			}

			List<Candle> candles = new ArrayList<>();

			for (HistoricalData item : historicalData.dataArrayList) {

				LocalDateTime candleTime = parseTimestamp(item.timeStamp);

				candles.add(

						Candle.builder()

								.instrumentToken(instrumentToken)

								.tradingSymbol(tradingSymbol)

								.exchange(exchange)

								.interval(interval)

								.tradeDate(candleTime.toLocalDate())

								.dateTime(candleTime)

								.open(BigDecimal.valueOf(item.open))

								.high(BigDecimal.valueOf(item.high))

								.low(BigDecimal.valueOf(item.low))

								.close(BigDecimal.valueOf(item.close))

								.volume(item.volume)

								.openInterest(item.oi)

								.averagePrice(BigDecimal.valueOf((item.high + item.low + item.close) / 3.0))

								.vwap(BigDecimal.ZERO)

								.tickCount(0)

								.completed(true)

								.build());

			}

			log.info("Loaded {} candles.", candles.size());

			return candles;

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to fetch historical data.", ex);

		}

	}

	/**
	 * Supports
	 *
	 * 2026-07-06T00:00:00+0530
	 *
	 * and
	 *
	 * 2026-07-06T00:00:00+05:30
	 */
	private LocalDateTime parseTimestamp(String timestamp) {

		try {

			return OffsetDateTime.parse(timestamp).toLocalDateTime();

		} catch (Exception ignored) {

		}

		return OffsetDateTime.parse(timestamp, KITE_FORMAT).toLocalDateTime();

	}

}