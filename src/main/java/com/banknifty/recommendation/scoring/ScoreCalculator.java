package com.banknifty.recommendation.scoring;

import com.banknifty.recommendation.model.OptionCandidate;

public interface ScoreCalculator {

	int score(OptionCandidate candidate);

}