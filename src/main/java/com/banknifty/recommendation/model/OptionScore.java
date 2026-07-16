package com.banknifty.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionScore {

	private Integer trendScore;

	private Integer optionChainScore;

	private Integer liquidityScore;

	private Integer strikeScore;

	private Integer expiryScore;

	private Integer riskRewardScore;

	private Integer volatilityScore;

	private Integer greekScore;

	private Integer totalScore;

	private Integer confidence;
}