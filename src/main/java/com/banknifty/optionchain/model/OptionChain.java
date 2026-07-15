package com.banknifty.optionchain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OptionChain(

		String underlying,

		LocalDate expiry,

		BigDecimal spotPrice,

		List<OptionStrike> calls,

		List<OptionStrike> puts

) {
}