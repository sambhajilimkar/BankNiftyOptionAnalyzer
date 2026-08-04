package com.banknifty.analysis.prediction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PredictionSummary {

	private final PredictionDirection direction;

	private final PredictionStrength strength;

	/**
	 * Prediction confidence (0-100)
	 */
	private final double confidence;

	/**
	 * Expected move in BankNifty points
	 */
	private final double expectedMove;

	/**
	 * Probability of trend continuation
	 */
	private final double continuationProbability;

	/**
	 * Probability of reversal
	 */
	private final double reversalProbability;
}