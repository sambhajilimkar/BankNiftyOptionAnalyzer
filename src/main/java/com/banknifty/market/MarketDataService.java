package com.banknifty.market;

import com.banknifty.model.Candle;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteOptionChainProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.OHLC;
import com.zerodhatech.models.Quote;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketDataService {

	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteQuoteProvider quoteProvider;
	private final KiteInstrumentProvider instrumentProvider;
	private final KiteOptionChainProvider optionChainProvider;

	public MarketDataService(KiteHistoricalDataProvider historicalDataProvider, KiteQuoteProvider quoteProvider,
			KiteInstrumentProvider instrumentProvider, KiteOptionChainProvider optionChainProvider) {

		this.historicalDataProvider = historicalDataProvider;
		this.quoteProvider = quoteProvider;
		this.instrumentProvider = instrumentProvider;
		this.optionChainProvider = optionChainProvider;
	}

	/**
	 * Historical candles.
	 */
	public List<Candle> getHistoricalCandles(Long instrumentToken, String tradingSymbol, String exchange,
			String interval, LocalDateTime from, LocalDateTime to) {

		return historicalDataProvider.fetchHistoricalData(instrumentToken, tradingSymbol, exchange, interval, from, to,
				false, true);
	}

	/**
	 * Latest Quote.
	 */
	public Quote getQuote(String instrument) throws KiteException, IOException, JSONException {

		return quoteProvider.getQuote(instrument);

	}

	/**
	 * Latest LTP.
	 */
	public BigDecimal getLtp(String instrument) throws KiteException, IOException, JSONException {

		return quoteProvider.getLTP(instrument);

	}

	/**
	 * Latest OHLC.
	 */
	public OHLC getOhlc(String instrument) throws KiteException, IOException, JSONException {

		return quoteProvider.getOHLC(instrument);

	}

	/**
	 * Resolve instrument token.
	 */
	public Long getInstrumentToken(String exchange, String tradingSymbol)
			throws KiteException, IOException, JSONException {

		return instrumentProvider.getInstrumentToken(exchange, tradingSymbol);

	}

	/**
	 * Instrument details.
	 */
	public Instrument getInstrument(String exchange, String tradingSymbol)
			throws KiteException, IOException, JSONException {

		return instrumentProvider.getInstrument(exchange, tradingSymbol);

	}

	/**
	 * BANKNIFTY option chain.
	 */
	public List<Instrument> getOptionChain(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return optionChainProvider.getOptionChain(exchange, underlying);

	}

	/**
	 * CE contracts.
	 */
	public List<Instrument> getCallOptions(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return optionChainProvider.getCallOptions(exchange, underlying);

	}

	/**
	 * PE contracts.
	 */
	public List<Instrument> getPutOptions(String exchange, String underlying)
			throws KiteException, IOException, JSONException {

		return optionChainProvider.getPutOptions(exchange, underlying);

	}

}