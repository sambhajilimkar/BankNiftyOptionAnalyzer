package com.banknifty.provider;

import com.banknifty.exception.GatewayException;
import com.banknifty.gateway.GatewayHealthService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.OHLC;
import com.zerodhatech.models.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiteQuoteProvider {

	private final KiteConnect kiteConnect;

	private final GatewayHealthService healthService;

	/**
	 * Complete Quote.
	 */
	public Quote getQuote(String instrument) {

		try {

			Map<String, Quote> quotes = null;
			try {
				quotes = kiteConnect.getQuote(new String[] { instrument });
			} catch (KiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Quote quote = quotes.get(instrument);

			healthService.markSuccess();

			return quote;

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to fetch Quote : " + instrument, ex);

		}

	}

	/**
	 * Live LTP.
	 */
	public BigDecimal getLTP(String instrument) {

		try {

			Map<String, LTPQuote> quotes = null;
			try {
				quotes = kiteConnect.getLTP(new String[] { instrument });
			} catch (KiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LTPQuote quote = quotes.get(instrument);

			healthService.markSuccess();

			if (quote == null) {
				return BigDecimal.ZERO;
			}

			return BigDecimal.valueOf(quote.lastPrice);

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to fetch LTP : " + instrument, ex);

		}

	}

	/**
	 * OHLC.
	 */
	public OHLC getOHLC(String instrument) {

		Quote quote = getQuote(instrument);

		return quote == null ? null : quote.ohlc;

	}

	/**
	 * Batch Quote.
	 */
	public Map<String, Quote> getQuotes(String... instruments) {

		try {

			Map<String, Quote> result = null;
			try {
				result = kiteConnect.getQuote(instruments);
			} catch (KiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			healthService.markSuccess();

			return result;

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to fetch Quotes.", ex);

		}

	}

	/**
	 * Batch LTP.
	 */
	public Map<String, LTPQuote> getLTPs(String... instruments) {

		try {

			Map<String, LTPQuote> result = null;
			try {
				result = kiteConnect.getLTP(instruments);
			} catch (KiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			healthService.markSuccess();

			return result;

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to fetch LTPs.", ex);

		}

	}

}