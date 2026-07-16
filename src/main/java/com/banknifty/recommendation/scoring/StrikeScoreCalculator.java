package com.banknifty.recommendation.scoring;

import com.banknifty.recommendation.model.OptionCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StrikeScoreCalculator implements ScoreCalculator {

	@Override
	public int score(OptionCandidate candidate) {

		BigDecimal distance = candidate.getDistanceFromATM();

		if (distance.compareTo(BigDecimal.valueOf(50)) <= 0) {
			return 20;
		}

		if (distance.compareTo(BigDecimal.valueOf(100)) <= 0) {
			return 18;
		}

		if (distance.compareTo(BigDecimal.valueOf(200)) <= 0) {
			return 15;
		}

		if (distance.compareTo(BigDecimal.valueOf(300)) <= 0) {
			return 10;
		}

		return 5;
	}

}