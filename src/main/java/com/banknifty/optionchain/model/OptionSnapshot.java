package com.banknifty.optionchain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Complete option market snapshot for one underlying.
 */
@Builder
public record OptionSnapshot(

		String underlying,

		LocalDate expiry,

		BigDecimal spotPrice,

		Integer atmStrike,

		Integer itmCallStrike,

		Integer itmPutStrike,

		Integer otmCallStrike,

		Integer otmPutStrike,

		OptionMetrics metrics,

		List<OptionStrike> calls,

		List<OptionStrike> puts

) {
}