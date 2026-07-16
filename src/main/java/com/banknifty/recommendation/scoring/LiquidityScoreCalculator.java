package com.banknifty.recommendation.scoring;

import com.banknifty.recommendation.model.OptionCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LiquidityScoreCalculator implements ScoreCalculator {

	@Override
	public int score(OptionCandidate candidate) {

		int score = 0;

		if (candidate.getOpenInterest() != null) {

			if (candidate.getOpenInterest() > 1_000_000L) {
				score += 10;
			} else if (candidate.getOpenInterest() > 500_000L) {
				score += 7;
			} else {
				score += 4;
			}
		}

		if (candidate.getVolume() != null) {

			if (candidate.getVolume() > 100_000L) {
				score += 10;
			} else if (candidate.getVolume() > 50_000L) {
				score += 7;
			} else {
				score += 4;
			}
		}

		if (candidate.getSpread() != null) {

			BigDecimal spread = candidate.getSpread();

			if (spread.compareTo(BigDecimal.ONE) <= 0) {
				score += 5;
			} else if (spread.compareTo(BigDecimal.valueOf(2)) <= 0) {
				score += 3;
			}
		}

		return Math.min(score, 25);
	}

}