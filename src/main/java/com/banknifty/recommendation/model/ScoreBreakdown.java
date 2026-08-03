package com.banknifty.recommendation.model;

/**
 * Detailed score breakdown produced by the ranking engine.
 */
public record ScoreBreakdown(

		double technical,

		double setup,

		double openInterest,

		double greeks,

		double liquidity,

		double riskReward,

		double finalScore,

		double trendPoints,

		double supportResistancePoints,

		double pivotPoints,

		double strikePoints,

		double expiryPoints,

		double volatilityPoints

) {

	public double highestContributor() {

		return Math.max(Math.max(technical, setup), Math.max(Math.max(openInterest, greeks),
				Math.max(Math.max(liquidity, riskReward), Math.max(trendPoints, Math.max(supportResistancePoints,
						Math.max(pivotPoints, Math.max(strikePoints, Math.max(expiryPoints, volatilityPoints))))))));
	}

	public double lowestContributor() {

		return Math.min(Math.min(technical, setup), Math.min(Math.min(openInterest, greeks),
				Math.min(Math.min(liquidity, riskReward), Math.min(trendPoints, Math.min(supportResistancePoints,
						Math.min(pivotPoints, Math.min(strikePoints, Math.min(expiryPoints, volatilityPoints))))))));
	}

	public boolean excellent() {
		return finalScore >= 95;
	}

	public boolean strong() {
		return finalScore >= 90;
	}

	public boolean tradable() {
		return finalScore >= 70;
	}

	public double normalizedScore() {

		if (finalScore < 0) {
			return 0;
		}

		if (finalScore > 100) {
			return 100;
		}

		return finalScore;
	}

	public String grade() {

		if (finalScore >= 95) {
			return "A+";
		}

		if (finalScore >= 90) {
			return "A";
		}

		if (finalScore >= 85) {
			return "B+";
		}

		if (finalScore >= 80) {
			return "B";
		}

		if (finalScore >= 70) {
			return "C";
		}

		return "REJECT";
	}

	public String strongestArea() {

		double highest = highestContributor();

		if (highest == technical)
			return "Technical";
		if (highest == setup)
			return "Setup";
		if (highest == openInterest)
			return "Open Interest";
		if (highest == greeks)
			return "Greeks";
		if (highest == liquidity)
			return "Liquidity";
		if (highest == riskReward)
			return "Risk Reward";
		if (highest == trendPoints)
			return "Trend";
		if (highest == supportResistancePoints)
			return "Support/Resistance";
		if (highest == pivotPoints)
			return "Pivot";
		if (highest == strikePoints)
			return "Strike";
		if (highest == expiryPoints)
			return "Expiry";

		return "Volatility";
	}

	public String weakestArea() {

		double lowest = lowestContributor();

		if (lowest == technical)
			return "Technical";
		if (lowest == setup)
			return "Setup";
		if (lowest == openInterest)
			return "Open Interest";
		if (lowest == greeks)
			return "Greeks";
		if (lowest == liquidity)
			return "Liquidity";
		if (lowest == riskReward)
			return "Risk Reward";
		if (lowest == trendPoints)
			return "Trend";
		if (lowest == supportResistancePoints)
			return "Support/Resistance";
		if (lowest == pivotPoints)
			return "Pivot";
		if (lowest == strikePoints)
			return "Strike";
		if (lowest == expiryPoints)
			return "Expiry";

		return "Volatility";
	}

}