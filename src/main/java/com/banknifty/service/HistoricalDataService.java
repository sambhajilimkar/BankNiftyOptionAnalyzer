package com.banknifty.service;

import com.banknifty.market.MarketDataService;
import com.banknifty.model.Candle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricalDataService {

	private final MarketDataService marketDataService;

	public HistoricalDataService(MarketDataService marketDataService) {
		this.marketDataService = marketDataService;
	}

	/**
	 * Returns historical candles.
	 */
	public List<Candle> getHistoricalCandles(Long instrumentToken, String tradingSymbol, String exchange,
			String interval, LocalDateTime from, LocalDateTime to) {

		return marketDataService.getHistoricalCandles(instrumentToken, tradingSymbol, exchange, interval, from, to);
	}

	/**
	 * Convenience method for BANKNIFTY.
	 */
	public List<Candle> getBankNiftyCandles(Long instrumentToken, LocalDateTime from, LocalDateTime to) {

		return getHistoricalCandles(instrumentToken, "BANKNIFTY", "NFO", "5minute", from, to);
	}

}