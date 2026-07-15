package com.banknifty.controller;

import com.banknifty.market.MarketDataService;
import com.banknifty.model.Candle;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.OHLC;
import com.zerodhatech.models.Quote;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

	private final MarketDataService marketDataService;

	public MarketController(MarketDataService marketDataService) {
		this.marketDataService = marketDataService;
	}

	@GetMapping("/historical")
	public List<Candle> historical(

			@RequestParam Long instrumentToken,

			@RequestParam String tradingSymbol,

			@RequestParam(defaultValue = "NFO") String exchange,

			@RequestParam(defaultValue = "5minute") String interval,

			@RequestParam LocalDateTime from,

			@RequestParam LocalDateTime to) {

		return marketDataService.getHistoricalCandles(

				instrumentToken,

				tradingSymbol,

				exchange,

				interval,

				from,

				to

		);

	}

	@GetMapping("/quote")
	public Quote quote(@RequestParam String instrument) throws KiteException, IOException, JSONException {

		return marketDataService.getQuote(instrument);

	}

	@GetMapping("/ltp")
	public BigDecimal ltp(@RequestParam String instrument) throws KiteException, IOException, JSONException {

		return marketDataService.getLtp(instrument);

	}

	@GetMapping("/ohlc")
	public OHLC ohlc(@RequestParam String instrument) throws KiteException, IOException, JSONException {

		return marketDataService.getOhlc(instrument);

	}

	@GetMapping("/instrument")
	public Instrument instrument(

			@RequestParam String exchange,

			@RequestParam String tradingSymbol)

			throws KiteException, IOException, JSONException {

		return marketDataService.getInstrument(exchange, tradingSymbol);

	}

	@GetMapping("/option-chain")
	public List<Instrument> optionChain(

			@RequestParam(defaultValue = "NFO") String exchange,

			@RequestParam(defaultValue = "BANKNIFTY") String underlying)

			throws KiteException, IOException, JSONException {

		return marketDataService.getOptionChain(exchange, underlying);

	}

}