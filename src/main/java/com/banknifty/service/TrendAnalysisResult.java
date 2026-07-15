package com.banknifty.service;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TrendAnalysisResult(

		RecommendationAction action,

		OptionType optionType,

		BigDecimal spotPrice,

		Integer confidence,

		BigDecimal ema20,

		BigDecimal ema50,

		BigDecimal rsi,

		BigDecimal macd,

		BigDecimal adx,

		BigDecimal vwap,

		List<String> reasons

) {
}