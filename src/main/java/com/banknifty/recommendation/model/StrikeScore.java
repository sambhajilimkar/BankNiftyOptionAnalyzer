package com.banknifty.recommendation.model;

import lombok.Builder;

@Builder
public record StrikeScore(

		StrikeCandidate strike,

		int trendScore,

		int oiScore,

		int volumeScore,

		int momentumScore,

		int liquidityScore,

		int greekScore,

		int supportScore,

		int expiryScore,

		int totalScore

) {
}