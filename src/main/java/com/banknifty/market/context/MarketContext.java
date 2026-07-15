package com.banknifty.market.context;

import lombok.Builder;

import java.util.List;

@Builder
public record MarketContext(

		boolean tradeAllowed,

		int confidenceAdjustment,

		int riskAdjustment,

		String marketSession,

		boolean expiryDay,

		boolean weeklyExpiry,

		boolean monthlyExpiry,

		boolean eventDay,

		boolean highVolatility,

		boolean globalBullish,

		boolean globalBearish,

		List<String> warnings

) {
}