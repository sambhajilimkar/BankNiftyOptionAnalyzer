package com.banknifty.recommendation.explain;

import lombok.Builder;

/**
 * Represents a single explanation item used by the recommendation engine.
 *
 * Each reason explains WHY a contract was selected or rejected and optionally
 * indicates how much it contributed to the final score.
 */
@Builder
public record RecommendationReason(

		/*
		 * Short title.
		 *
		 * Example: EMA Alignment Bullish Breakout Long Build-up
		 */
		String title,

		/*
		 * Detailed explanation.
		 */
		String description,

		/*
		 * Score contribution.
		 *
		 * Positive: +20
		 *
		 * Negative: -10
		 */
		int contribution,

		/*
		 * Importance (0-100)
		 */
		int importance

) {

	/**
	 * Positive contribution.
	 */
	public boolean positive() {
		return contribution > 0;
	}

	/**
	 * Negative contribution.
	 */
	public boolean negative() {
		return contribution < 0;
	}

	/**
	 * Neutral contribution.
	 */
	public boolean neutral() {
		return contribution == 0;
	}

	/**
	 * High impact reason.
	 */
	public boolean major() {
		return importance >= 80;
	}
}