package com.banknifty.recommendation.scoring;

import com.banknifty.recommendation.model.OptionCandidate;
import com.banknifty.recommendation.model.OptionScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionScoreCalculator {

	private final StrikeScoreCalculator strikeScoreCalculator;

	private final LiquidityScoreCalculator liquidityScoreCalculator;

	private final ExpiryScoreCalculator expiryScoreCalculator;

	public OptionScore calculate(OptionCandidate candidate, int trendScore, int riskRewardScore) {

		int strikeScore = strikeScoreCalculator.score(candidate);

		int liquidityScore = liquidityScoreCalculator.score(candidate);

		int expiryScore = expiryScoreCalculator.score(candidate);

		int total = trendScore + strikeScore + liquidityScore + expiryScore + riskRewardScore;

		total = Math.min(total, 100);

		return OptionScore.builder()

				.trendScore(trendScore)

				.strikeScore(strikeScore)

				.liquidityScore(liquidityScore)

				.expiryScore(expiryScore)

				.riskRewardScore(riskRewardScore)

				.optionChainScore(0)

				.volatilityScore(0)

				.greekScore(0)

				.confidence(total)

				.totalScore(total)

				.build();

	}

}