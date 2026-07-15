package com.banknifty.broker.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record LiveQuote(

		Long instrumentToken,

		String tradingSymbol,

		BigDecimal ltp,

		BigDecimal open,

		BigDecimal high,

		BigDecimal low,

		BigDecimal close,

		Long volume,

		LocalDateTime time

) {
}