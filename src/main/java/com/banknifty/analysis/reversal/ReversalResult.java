package com.banknifty.analysis.reversal;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class ReversalResult {

	/**
	 * Total reversal score (0-100).
	 */
	private double score;

	/**
	 * Highest score contributed by a single detector. Used to decide the final
	 * reversal direction.
	 */
	private double strongestSignal;

	/**
	 * Final predicted reversal direction.
	 */
	private ReversalDirection direction = ReversalDirection.NONE;

	/**
	 * Reasons contributing to the reversal.
	 */
	private final List<ReversalReason> reasons = new ArrayList<>();

	/**
	 * Add weighted score.
	 */
	public void addScore(double value) {

		score += value;

		if (score > 100) {
			score = 100;
		}
	}

	/**
	 * Set direction only if this detector has stronger confidence than previous
	 * detectors.
	 */
	public void updateDirection(double detectorScore, ReversalDirection direction) {

		if (direction == null || direction == ReversalDirection.NONE) {
			return;
		}

		if (detectorScore >= strongestSignal) {

			strongestSignal = detectorScore;

			this.direction = direction;
		}
	}

	/**
	 * Backward compatible.
	 */
	public void setDirection(ReversalDirection direction) {

		updateDirection(score, direction);
	}

	/**
	 * Prevent duplicate reasons.
	 */
	public void addReason(ReversalReason reason) {

		if (reason == null) {
			return;
		}

		if (!reasons.contains(reason)) {
			reasons.add(reason);
		}
	}

	/**
	 * Convenience method.
	 */
	public boolean hasReversal() {

		return score >= 60;
	}

	/**
	 * Reset for reuse.
	 */
	public void reset() {

		score = 0;
		strongestSignal = 0;
		direction = ReversalDirection.NONE;
		reasons.clear();
	}
}