package com.banknifty.analysis.reversal;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReversalAnalysis {

	/**
	 * Final reversal decision.
	 */
	private final boolean reversalDetected;

	/**
	 * Current market trend.
	 */
	private final ReversalDirection currentTrend;

	/**
	 * Expected trend after reversal.
	 */
	private final ReversalDirection predictedTrend;

	/**
	 * Probability that current trend reverses.
	 */
	private final double reversalProbability;

	/**
	 * Probability that current trend continues.
	 */
	private final double continuationProbability;

	/**
	 * Overall reversal strength.
	 */
	private final ReversalStrength strength;

	/**
	 * Reasons contributing to the prediction.
	 */
	@Builder.Default
	private final List<ReversalReason> reasons = new ArrayList<>();

	/**
	 * Convenience method.
	 */
	public boolean bullishReversal() {
		return predictedTrend == ReversalDirection.BULLISH;
	}

	/**
	 * Convenience method.
	 */
	public boolean bearishReversal() {
		return predictedTrend == ReversalDirection.BEARISH;
	}

	/**
	 * Safe probability (0-100).
	 */
	public int reversalPercent() {
		return (int) Math.round(reversalProbability);
	}

	/**
	 * Safe probability (0-100).
	 */
	public int continuationPercent() {
		return (int) Math.round(continuationProbability);
	}

	/**
	 * High confidence reversal.
	 */
	public boolean highConfidence() {
		return reversalProbability >= 75;
	}

	/**
	 * Moderate confidence reversal.
	 */
	public boolean moderateConfidence() {
		return reversalProbability >= 60;
	}

	/**
	 * Low confidence reversal.
	 */
	public boolean lowConfidence() {
		return reversalProbability < 40;
	}

	/**
	 * Human readable summary.
	 */
	public String summary() {

		return String.format("%s | Reversal %.0f%% | Continuation %.0f%%", strength, reversalProbability,
				continuationProbability);
	}
}