package com.banknifty.broker;

import com.banknifty.broker.model.LiveQuote;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.OptionType;
import com.banknifty.market.instrument.InstrumentRegistry;
import com.banknifty.model.Candle;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.LTPQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KiteBrokerProvider implements BrokerProvider {

	private static final String NSE = "NSE";
	private static final String NFO = "NFO";
	private static final int CONTRACTS_PER_SIDE = 10;

	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteQuoteProvider quoteProvider;
	private final InstrumentRegistry instrumentRegistry;

	@Override
	public String brokerName() {
		return "ZERODHA";
	}

	@Override
	public boolean isConnected() {
		return instrumentRegistry.size() > 0;
	}

	@Override
	public List<Candle> historicalData(Long instrumentToken, String interval, LocalDateTime from, LocalDateTime to) {

		Instrument instrument = instrument(instrumentToken);
		return historicalDataProvider.fetchHistoricalData(instrumentToken, instrument.tradingsymbol,
				exchangeFor(instrument), interval, from, to, false, false);
	}

	@Override
	public LiveQuote quote(Long instrumentToken) {
		Instrument instrument = instrument(instrumentToken);
		BigDecimal ltp = quoteProvider.getLTP(exchangeFor(instrument) + ":" + instrument.tradingsymbol);

		return LiveQuote.builder().instrumentToken(instrumentToken).tradingSymbol(instrument.tradingsymbol).ltp(ltp)
				.open(BigDecimal.ZERO).high(BigDecimal.ZERO).low(BigDecimal.ZERO).close(BigDecimal.ZERO).volume(0L)
				.time(LocalDateTime.now()).build();
	}

	@Override
	public List<OptionQuote> optionChain(String underlying, String expiry) {
		BigDecimal spotPrice = spotPrice(underlying);
		Date selectedExpiry = selectExpiry(underlying, expiry);

		List<Instrument> contracts = instrumentRegistry.getOptions(underlying, selectedExpiry).stream()
				.filter(contract -> contract.instrument_type != null)
				.filter(contract -> contract.instrument_type.equalsIgnoreCase("CE")
						|| contract.instrument_type.equalsIgnoreCase("PE"))
				.sorted(Comparator.comparing(contract -> strikeDistance(contract, spotPrice)))
				.limit(CONTRACTS_PER_SIDE * 2L).toList();

		if (contracts.isEmpty()) {
			return List.of();
		}

		String[] instruments = contracts.stream().map(contract -> NFO + ":" + contract.tradingsymbol)
				.toArray(String[]::new);
		Map<String, LTPQuote> quotes = quoteProvider.getLTPs(instruments);

		return contracts.stream()
				.map(contract -> toOptionQuote(contract, quotes.get(NFO + ":" + contract.tradingsymbol)))
				.filter(quote -> quote.ltp().signum() > 0).toList();
	}

	private BigDecimal spotPrice(String underlying) {
		String spotSymbol = "BANKNIFTY".equalsIgnoreCase(underlying) ? "NIFTY BANK" : underlying;
		BigDecimal ltp = quoteProvider.getLTP(NSE + ":" + spotSymbol);
		if (ltp == null || ltp.signum() <= 0) {
			throw new IllegalStateException("No live index price is available for " + underlying);
		}
		return ltp;
	}

	private Date selectExpiry(String underlying, String expiryType) {
		List<Date> expiries = instrumentRegistry.getExpiries(underlying);
		if (expiries.isEmpty()) {
			throw new IllegalStateException("No option expiries are available for " + underlying);
		}

		if (!ExpiryType.MONTHLY.name().equalsIgnoreCase(expiryType)) {
			return expiries.getFirst();
		}

		YearMonth currentMonth = YearMonth.now();
		return expiries.stream().filter(expiry -> YearMonth.from(toLocalDate(expiry)).equals(currentMonth))
				.max(Comparator.naturalOrder()).orElse(expiries.getFirst());
	}

	private BigDecimal strikeDistance(Instrument instrument, BigDecimal spotPrice) {
		return BigDecimal.valueOf(Double.parseDouble(instrument.strike)).subtract(spotPrice).abs();
	}

	private OptionQuote toOptionQuote(Instrument instrument, LTPQuote quote) {
		BigDecimal ltp = quote == null ? BigDecimal.ZERO : BigDecimal.valueOf(quote.lastPrice);

		return OptionQuote.builder().instrumentToken(instrument.instrument_token)
				.tradingSymbol(instrument.tradingsymbol).strike((int) Math.round(Double.parseDouble(instrument.strike)))
				.expiry(toLocalDate(instrument.expiry))
				.optionType("CE".equalsIgnoreCase(instrument.instrument_type) ? OptionType.CE : OptionType.PE).ltp(ltp)
				.volume(0L).openInterest(0L).bid(BigDecimal.ZERO).ask(BigDecimal.ZERO).iv(BigDecimal.ZERO)
				.delta(BigDecimal.ZERO).theta(BigDecimal.ZERO).gamma(BigDecimal.ZERO).vega(BigDecimal.ZERO).build();
	}

	private Instrument instrument(Long instrumentToken) {
		Instrument instrument = instrumentRegistry.getByToken(instrumentToken);
		if (instrument == null) {
			throw new IllegalArgumentException("Unknown instrument token: " + instrumentToken);
		}
		return instrument;
	}

	private String exchangeFor(Instrument instrument) {
		return instrument.segment != null && instrument.segment.startsWith("NFO") ? NFO : NSE;
	}

	private LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
