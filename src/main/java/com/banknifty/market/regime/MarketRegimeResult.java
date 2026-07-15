package com.banknifty.market.regime;

import lombok.Builder;

import java.util.List;

@Builder
public record MarketRegimeResult(

		MarketRegime regime,

		int confidence,

		boolean tradeAllowed,

		List<String> reasons

) {
}