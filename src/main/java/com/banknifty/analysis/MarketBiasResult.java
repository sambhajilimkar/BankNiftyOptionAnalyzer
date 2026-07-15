package com.banknifty.analysis;

import lombok.Builder;

import java.util.List;

@Builder
public record MarketBiasResult(

		MarketBias bias,

		int confidence,

		List<String> reasons

) {
}