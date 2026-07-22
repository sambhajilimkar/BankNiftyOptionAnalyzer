package com.banknifty.optionchain.mapper;

import java.util.List;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.optionchain.model.OptionStrike;

public interface OptionStrikeMapper {

	/**
	 * Convert broker OptionQuote to domain OptionStrike.
	 */
	OptionStrike toStrike(OptionQuote quote);

	/**
	 * Convert complete broker option chain.
	 */
	List<OptionStrike> toStrikes(List<OptionQuote> quotes);

}