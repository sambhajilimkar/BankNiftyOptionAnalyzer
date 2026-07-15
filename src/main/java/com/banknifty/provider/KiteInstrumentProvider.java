package com.banknifty.provider;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Provider for Zerodha Instruments.
 */
@Component
public class KiteInstrumentProvider {

	private final KiteConnect kiteConnect;

	public KiteInstrumentProvider(KiteConnect kiteConnect) {
		this.kiteConnect = kiteConnect;
	}

	/**
	 * Fetch all instruments for an exchange.
	 */
	public List<Instrument> getInstruments(String exchange) throws KiteException, IOException, JSONException {

		return kiteConnect.getInstruments(exchange);
	}

	/**
	 * Resolve instrument token by trading symbol.
	 */
	public Long getInstrumentToken(String exchange, String tradingSymbol)
			throws KiteException, IOException, JSONException {

		List<Instrument> instruments = getInstruments(exchange);

		for (Instrument instrument : instruments) {
			if (tradingSymbol.equalsIgnoreCase(instrument.tradingsymbol)) {
				return instrument.instrument_token;
			}
		}

		throw new IllegalArgumentException("Instrument not found : " + tradingSymbol);
	}

	/**
	 * Resolve Instrument object.
	 */
	public Instrument getInstrument(String exchange, String tradingSymbol)
			throws KiteException, IOException, JSONException {

		List<Instrument> instruments = getInstruments(exchange);

		return instruments.stream().filter(i -> tradingSymbol.equalsIgnoreCase(i.tradingsymbol)).findFirst()
				.orElse(null);
	}
}
