package com.banknifty.gateway;

import com.banknifty.model.Candle;
import com.zerodhatech.models.OHLC;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface KiteGateway {

	/**
	 * Generate Kite session using request token.
	 */
	User generateSession(String requestToken) throws Exception;

	/**
	 * Configure an existing access token.
	 */
	void configureSession(String accessToken, String userId);

	/**
	 * Returns current LTP.
	 */
	BigDecimal getLTP(String instrument) throws Exception;

	/**
	 * Returns complete quote.
	 */
	Quote getQuote(String instrument) throws Exception;

	/**
	 * Returns OHLC.
	 */
	OHLC getOHLC(String instrument) throws Exception;

	/**
	 * Batch Quote API.
	 */
	Map<String, Quote> getQuotes(String... instruments) throws Exception;

	/**
	 * Batch LTP API.
	 */
	Map<String, com.zerodhatech.models.LTPQuote> getLTPs(String... instruments) throws Exception;

	/**
	 * Historical candles.
	 */
	List<Candle> getHistoricalData(Long instrumentToken, String tradingSymbol, String exchange, String interval,
			LocalDateTime from, LocalDateTime to, boolean continuous, boolean openInterest);

}