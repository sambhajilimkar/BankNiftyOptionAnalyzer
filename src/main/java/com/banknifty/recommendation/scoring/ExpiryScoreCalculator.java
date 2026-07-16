package com.banknifty.recommendation.scoring;

import com.banknifty.recommendation.model.OptionCandidate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ExpiryScoreCalculator implements ScoreCalculator {

	@Override
	public int score(OptionCandidate candidate) {

		long days = ChronoUnit.DAYS.between(LocalDate.now(), candidate.getExpiry());

		if (days <= 1) {
			return 10;
		}

		if (days <= 3) {
			return 8;
		}

		if (days <= 7) {
			return 6;
		}

		return 4;

	}

}