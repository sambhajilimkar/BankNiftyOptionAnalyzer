package com.banknifty.market.instrument;

import com.banknifty.provider.KiteInstrumentProvider;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import jakarta.annotation.PostConstruct;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InstrumentRegistry {

	private final KiteInstrumentProvider instrumentProvider;

	/**
	 * token -> instrument
	 */
	private final Map<Long, Instrument> tokenMap = new ConcurrentHashMap<>();

	/**
	 * symbol -> instrument
	 */
	private final Map<String, Instrument> symbolMap = new ConcurrentHashMap<>();

	/**
	 * all instruments
	 */
	private final List<Instrument> instruments = new ArrayList<>();

	public InstrumentRegistry(KiteInstrumentProvider instrumentProvider) {
		this.instrumentProvider = instrumentProvider;
	}

	@PostConstruct
	public void initialize() throws KiteException, IOException, JSONException {

		loadExchange("NFO");
		loadExchange("NSE");

	}

	private void loadExchange(String exchange) throws KiteException, IOException, JSONException {

		List<Instrument> list = instrumentProvider.getInstruments(exchange);

		for (Instrument instrument : list) {

			tokenMap.put(instrument.instrument_token, instrument);

			symbolMap.put(instrument.tradingsymbol, instrument);

			instruments.add(instrument);

		}

	}

	public Instrument getByToken(Long token) {

		return tokenMap.get(token);

	}

	public Instrument getByTradingSymbol(String symbol) {

		return symbolMap.get(symbol);

	}

	/**
	 * Returns all instruments.
	 */
	public List<Instrument> getAll() {

		return Collections.unmodifiableList(instruments);

	}

	/**
	 * Returns all option contracts for an underlying.
	 *
	 * Example: BANKNIFTY NIFTY FINNIFTY
	 */
	public List<Instrument> getOptions(String underlying) {

		return instruments.stream()

				.filter(i -> i.segment != null)

				.filter(i -> i.segment.equalsIgnoreCase("NFO-OPT"))

				.filter(i -> i.tradingsymbol.startsWith(underlying))

				.collect(Collectors.toList());

	}

	/**
	 * Returns all option contracts for a specific expiry.
	 */
	public List<Instrument> getOptions(String underlying, Date expiry) {

		return getOptions(underlying)

				.stream()

				.filter(i -> expiry.equals(i.expiry))

				.collect(Collectors.toList());

	}

	/**
	 * Returns nearest expiry available.
	 */
	public Optional<Date> getNearestExpiry(String underlying) {

		return getOptions(underlying)

				.stream()

				.map(i -> i.expiry)

				.filter(Objects::nonNull)

				.distinct()

				.sorted()

				.findFirst();

	}

	/**
	 * Returns all expiries.
	 */
	public List<Date> getExpiries(String underlying) {

		return getOptions(underlying)

				.stream()

				.map(i -> i.expiry)

				.filter(Objects::nonNull)

				.distinct()

				.sorted()

				.collect(Collectors.toList());

	}

	public boolean contains(Long token) {

		return tokenMap.containsKey(token);

	}

	public int size() {

		return tokenMap.size();

	}

}