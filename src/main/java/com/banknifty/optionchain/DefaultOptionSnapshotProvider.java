package com.banknifty.optionchain;

import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.OptionType;
import com.banknifty.market.instrument.InstrumentRegistry;
import com.banknifty.market.websocket.LiveMarketStore;
import com.banknifty.optionchain.model.OptionMetrics;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Tick;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultOptionSnapshotProvider implements OptionSnapshotProvider {

	private final InstrumentRegistry instrumentRegistry;

	private final LiveMarketStore liveMarketStore;

	@Override
	public OptionSnapshot load(String underlying, String expiryType) {

		Date expiry = resolveExpiry(underlying, expiryType);

		List<Instrument> instruments = instrumentRegistry.getOptions(underlying, expiry);

		List<OptionStrike> calls = new ArrayList<>();
		List<OptionStrike> puts = new ArrayList<>();

		BigDecimal spotPrice = findSpotPrice(underlying);

		for (Instrument instrument : instruments) {

			Tick tick = liveMarketStore.getTick(instrument.instrument_token);

			if (tick == null)
				continue;

			OptionStrike strike = OptionStrike.builder()

					.instrumentToken(instrument.instrument_token)

					.tradingSymbol(instrument.tradingsymbol)

					.strike((int) Math.round(Double.parseDouble(instrument.strike)))

					.expiry(toLocalDate(instrument.expiry))

					.optionType(instrument.instrument_type.equalsIgnoreCase("CE") ? OptionType.CE : OptionType.PE)

					.ltp(BigDecimal.valueOf(tick.getLastTradedPrice()))

					.volume(tick.getVolumeTradedToday())

					.openInterest((long) tick.getOi())

					.bid(BigDecimal.ZERO)

					.ask(BigDecimal.ZERO)

					.iv(BigDecimal.ZERO)

					.delta(BigDecimal.ZERO)

					.theta(BigDecimal.ZERO)

					.gamma(BigDecimal.ZERO)

					.vega(BigDecimal.ZERO)

					.build();

			if (strike.optionType() == OptionType.CE)
				calls.add(strike);
			else
				puts.add(strike);

		}

		calls.sort(Comparator.comparing(OptionStrike::strike));
		puts.sort(Comparator.comparing(OptionStrike::strike));

		return OptionSnapshot.builder()

				.underlying(underlying)

				.expiry(toLocalDate(expiry))

				.spotPrice(spotPrice)

				.atmStrike(findATM(spotPrice))

				.metrics(OptionMetrics.builder().build())

				.calls(calls)

				.puts(puts)

				.build();

	}

	private Date resolveExpiry(String underlying, String expiryType) {

		List<Date> expiries = instrumentRegistry.getExpiries(underlying);

		if (expiries.isEmpty())
			throw new IllegalStateException("No expiry available");

		if (ExpiryType.MONTHLY.name().equalsIgnoreCase(expiryType)) {

			return expiries.get(expiries.size() - 1);

		}

		return expiries.get(0);

	}

	private BigDecimal findSpotPrice(String underlying) {

		Instrument instrument = instrumentRegistry.getByTradingSymbol(underlying);

		if (instrument == null)
			return BigDecimal.ZERO;

		Tick tick = liveMarketStore.getTick(instrument.instrument_token);

		if (tick == null)
			return BigDecimal.ZERO;

		return BigDecimal.valueOf(tick.getLastTradedPrice());

	}

	private Integer findATM(BigDecimal spot) {

		int value = spot.intValue();

		return (int) (Math.round(value / 100.0) * 100);

	}

	private java.time.LocalDate toLocalDate(Date date) {

		return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

	}

}