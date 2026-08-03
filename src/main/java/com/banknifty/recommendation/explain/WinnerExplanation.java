package com.banknifty.recommendation.explain;

import lombok.Builder;

import java.util.List;

/**
 * Human-readable explanation for the winning recommendation.
 *
 * This object explains WHY the recommendation engine selected this contract
 * over all other analysed contracts.
 */
@Builder
public record WinnerExplanation(

		/*
		 * Trading Symbol
		 *
		 * Example: BANKNIFTY25AUG57200CE
		 */
		String tradingSymbol,

		/*
		 * Final Recommendation Score
		 */
		int finalScore,

		/*
		 * Recommendation Grade
		 */
		RecommendationGrade grade,

		/*
		 * Confidence (0-100)
		 */
		int confidence,

		/*
		 * Complete score breakdown.
		 */
		ScoreBreakdown scoreBreakdown,

		/*
		 * Top reasons why this contract became Winner.
		 */
		List<String> strengths,

		/*
		 * Minor weaknesses (if any).
		 */
		List<String> weaknesses,

		/*
		 * One-line explanation for UI.
		 */
		String summary

) {

	/**
	 * Winner should always be tradable.
	 */
	public boolean tradable() {
		return grade != null && grade.tradable();
	}

	/**
	 * Returns true when this is an excellent trade.
	 */
	public boolean excellentTrade() {
		return grade == RecommendationGrade.A_PLUS;
	}

	/**
	 * Returns true when this is a strong buy.
	 */
	public boolean strongBuy() {
		return grade == RecommendationGrade.A;
	}

	/**
	 * Returns true when confirmation is still required.
	 */
	public boolean needsConfirmation() {

		return grade == RecommendationGrade.B || grade == RecommendationGrade.C;
	}

	/**
	 * Returns the primary strength.
	 */
	public String primaryStrength() {

		if (strengths == null || strengths.isEmpty()) {
			return "";
		}

		return strengths.get(0);
	}

	/**
	 * Returns the primary weakness.
	 */
	public String primaryWeakness() {

		if (weaknesses == null || weaknesses.isEmpty()) {
			return "";
		}

		return weaknesses.get(0);
	}
}