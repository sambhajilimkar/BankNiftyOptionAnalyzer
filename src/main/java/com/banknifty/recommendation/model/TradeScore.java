package com.banknifty.recommendation.model;

import lombok.Builder;

import java.util.List;

@Builder
public record TradeScore(

		int trendScore,

		int momentumScore,

		int optionChainScore,

		int oiScore,

		int marketScore,

		int riskScore,

		int totalScore,

		List<String> reasons

) {
}