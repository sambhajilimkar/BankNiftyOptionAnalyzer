package com.banknifty.optionchain.model;

import com.banknifty.enums.OptionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record OptionStrike(

		Long instrumentToken,

		String tradingSymbol,

		Integer strike,

		LocalDate expiry,

		OptionType optionType,

		BigDecimal ltp,

		Long volume,

		Long openInterest,

		BigDecimal bid,

		BigDecimal ask,

		BigDecimal iv,

		BigDecimal delta,

		BigDecimal theta,

		BigDecimal gamma,

		BigDecimal vega

) {
}