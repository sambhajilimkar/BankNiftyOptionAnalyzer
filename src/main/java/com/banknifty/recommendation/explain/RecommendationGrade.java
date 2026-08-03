package com.banknifty.recommendation.explain;

/**
 * Recommendation quality grade.
 *
 * This grade is derived from the final recommendation score and provides a
 * user-friendly representation of trade quality.
 */
public enum RecommendationGrade {

	A_PLUS("A+", "Excellent", "Highest probability trade"),

	A("A", "Strong Buy", "Very high quality trade"),

	B_PLUS("B+", "Buy", "Good quality trade"),

	B("B", "Watch", "Needs confirmation"),

	C("C", "Weak", "Low conviction"),

	REJECT("REJECT", "Reject", "Do not trade");

	private final String grade;
	private final String label;
	private final String description;

	RecommendationGrade(String grade, String label, String description) {

		this.grade = grade;
		this.label = label;
		this.description = description;
	}

	public String grade() {
		return grade;
	}

	public String label() {
		return label;
	}

	public String description() {
		return description;
	}

	public boolean tradable() {
		return this != REJECT;
	}
}