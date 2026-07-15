package com.banknifty.recommendation.model;

import com.banknifty.enums.OptionType;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Represents one strike under evaluation.
 */
@Builder
public record StrikeCandidate(

		String tradingSymbol,

		Long instrumentToken,

		Integer strike,

		OptionType optionType,

		BigDecimal ltp,

		Long openInterest,

		Long volume,

		BigDecimal iv,

		BigDecimal delta,

		BigDecimal theta,

		BigDecimal gamma,

		BigDecimal vega

) {
}