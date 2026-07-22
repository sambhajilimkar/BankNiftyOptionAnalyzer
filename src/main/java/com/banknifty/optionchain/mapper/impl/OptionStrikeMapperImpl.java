package com.banknifty.optionchain.mapper.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.optionchain.mapper.OptionStrikeMapper;
import com.banknifty.optionchain.model.OptionStrike;

@Component
public class OptionStrikeMapperImpl implements OptionStrikeMapper {

	@Override
	public OptionStrike toStrike(OptionQuote quote) {

		if (quote == null) {
			return null;
		}

		return OptionStrike.builder().instrumentToken(quote.instrumentToken()).tradingSymbol(quote.tradingSymbol())
				.strike(quote.strike()).expiry(quote.expiry()).optionType(quote.optionType()).ltp(quote.ltp())
				.volume(quote.volume()).openInterest(quote.openInterest()).bid(quote.bid()).ask(quote.ask())
				.iv(quote.iv()).delta(quote.delta()).theta(quote.theta()).gamma(quote.gamma()).vega(quote.vega())
				.build();
	}

	@Override
	public List<OptionStrike> toStrikes(List<OptionQuote> quotes) {

		if (quotes == null || quotes.isEmpty()) {
			return Collections.emptyList();
		}

		return quotes.stream().map(this::toStrike).toList();
	}

}