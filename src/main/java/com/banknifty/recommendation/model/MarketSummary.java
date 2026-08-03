package com.banknifty.recommendation.model;

import java.math.BigDecimal;

import com.banknifty.analysis.MarketBias;
import com.banknifty.market.regime.MarketRegime;

/** Direction, regime and option-chain context used for the recommendation. */
public record MarketSummary(BigDecimal spotPrice, MarketBias technicalBias, int technicalConfidence,
		MarketBias institutionalBias, double institutionalConfidence, MarketRegime regime,
		BigDecimal putCallRatio, Integer maxPainStrike, Integer supportStrike,
		Integer resistanceStrike, String setup) {
}
