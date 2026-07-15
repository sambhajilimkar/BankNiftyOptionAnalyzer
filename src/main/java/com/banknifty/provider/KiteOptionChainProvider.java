package com.banknifty.provider;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Option chain helper built on Kite instruments.
 */
@Component
public class KiteOptionChainProvider {

	private final KiteInstrumentProvider instrumentProvider;

	public KiteOptionChainProvider(KiteInstrumentProvider instrumentProvider) {
		this.instrumentProvider = instrumentProvider;
	}

	/**
	 * Returns all option contracts for the supplied underlying.
	 *
	 * Example: BANKNIFTY NIFTY FINNIFTY
	 */
	public List<Instrument> getOptionContracts(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return instrumentProvider.getInstruments(exchange).stream().filter(i -> i.tradingsymbol != null)
				.filter(i -> i.tradingsymbol.startsWith(underlying)).collect(Collectors.toList());
	}

	/**
	 * Returns CE contracts only.
	 */
	public List<Instrument> getCallOptions(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return getOptionContracts(exchange, underlying).stream().filter(i -> i.tradingsymbol.endsWith("CE"))
				.collect(Collectors.toList());
	}

	/**
	 * Returns PE contracts only.
	 */
	public List<Instrument> getPutOptions(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return getOptionContracts(exchange, underlying).stream().filter(i -> i.tradingsymbol.endsWith("PE"))
				.collect(Collectors.toList());
	}

	/**
	 * Find a specific strike.
	 */
	public Instrument findByTradingSymbol(String exchange, String tradingSymbol)
			throws KiteException, IOException, JSONException {

		return instrumentProvider.getInstrument(exchange, tradingSymbol);
	}
	
	/**
	 * Alias for getOptionContracts().
	 */
	public List<Instrument> getOptionChain(
	        String exchange,
	        String underlying)
	        throws KiteException, IOException, JSONException {

	    return getOptionContracts(exchange, underlying);

	}
}
