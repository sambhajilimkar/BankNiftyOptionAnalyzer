package com.banknifty.recommendation.explain;

/**
 * Utility class for converting recommendation score into user friendly
 * recommendation grades.
 */
public final class RecommendationGradeUtil {

	private RecommendationGradeUtil() {
	}

	/**
	 * Returns grade based on final recommendation score.
	 *
	 * Score Range 95-100 : A+ 90-94 : A 85-89 : B+ 80-84 : B 70-79 : C <70 : REJECT
	 */
	public static RecommendationGrade fromScore(int score) {

		if (score >= 95) {
			return RecommendationGrade.A_PLUS;
		}

		if (score >= 90) {
			return RecommendationGrade.A;
		}

		if (score >= 85) {
			return RecommendationGrade.B_PLUS;
		}

		if (score >= 80) {
			return RecommendationGrade.B;
		}

		if (score >= 70) {
			return RecommendationGrade.C;
		}

		return RecommendationGrade.REJECT;
	}

	/**
	 * Returns true when the recommendation is good enough for execution.
	 */
	public static boolean isTradable(int score) {
		return fromScore(score).tradable();
	}

	/**
	 * Returns grade string (A+, A, B+, ...).
	 */
	public static String grade(int score) {
		return fromScore(score).grade();
	}

	/**
	 * Returns user friendly label.
	 */
	public static String label(int score) {
		return fromScore(score).label();
	}

	/**
	 * Returns detailed description.
	 */
	public static String description(int score) {
		return fromScore(score).description();
	}
}