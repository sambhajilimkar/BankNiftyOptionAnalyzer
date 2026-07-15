package com.banknifty.gateway;

import com.banknifty.exception.GatewayException;
import com.banknifty.model.Candle;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.provider.KiteSessionProvider;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.OHLC;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiteGatewayImpl implements KiteGateway {

	private final KiteSessionProvider sessionProvider;
	private final KiteQuoteProvider quoteProvider;
	private final KiteHistoricalDataProvider historicalDataProvider;

	@Override
	public User generateSession(String requestToken) {

		try {
			return sessionProvider.generateSession(requestToken);
		} catch (Exception ex) {
			throw new GatewayException("Unable to generate Kite session.", ex);
		}

	}

	@Override
	public void configureSession(String accessToken, String userId) {

		sessionProvider.configureSession(accessToken, userId);

	}

	@Override
	public BigDecimal getLTP(String instrument) {

		try {
			return quoteProvider.getLTP(instrument);
		} catch (Exception ex) {
			throw new GatewayException("Unable to fetch LTP : " + instrument, ex);
		}

	}

	@Override
	public Quote getQuote(String instrument) {

		try {
			return quoteProvider.getQuote(instrument);
		} catch (Exception ex) {
			throw new GatewayException("Unable to fetch Quote : " + instrument, ex);
		}

	}

	@Override
	public OHLC getOHLC(String instrument) {

		try {
			return quoteProvider.getOHLC(instrument);
		} catch (Exception ex) {
			throw new GatewayException("Unable to fetch OHLC : " + instrument, ex);
		}

	}

	@Override
	public Map<String, Quote> getQuotes(String... instruments) {

		try {
			return quoteProvider.getQuotes(instruments);
		} catch (Exception ex) {
			throw new GatewayException("Unable to fetch Quotes.", ex);
		}

	}

	@Override
	public Map<String, LTPQuote> getLTPs(String... instruments) {

		try {
			return quoteProvider.getLTPs(instruments);
		} catch (Exception ex) {
			throw new GatewayException("Unable to fetch LTPs.", ex);
		}

	}

	@Override
	public List<Candle> getHistoricalData(Long instrumentToken, String tradingSymbol, String exchange, String interval,
			LocalDateTime from, LocalDateTime to, boolean continuous, boolean openInterest) {

		return historicalDataProvider.fetchHistoricalData(instrumentToken, tradingSymbol, exchange, interval, from, to,
				continuous, openInterest);

	}

}