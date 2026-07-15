package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.StrikeCandidate;
import com.banknifty.recommendation.model.StrikeScore;

import java.util.List;

public interface StrikeRankingEngine {

	List<StrikeScore> rank(List<StrikeCandidate> strikes);

}