package com.banknifty.recommendation.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.banknifty.enums.OptionType;

/**
 * A ranked option contract selected by the recommendation engine.
 */
public record RankedContract(

		int rank,

		String tradingSymbol,

		LocalDate expiryDate,

		OptionType optionType,

		Integer strikePrice,

		BigDecimal premium,

		double score,

		double confidence,

		BigDecimal entry,

		BigDecimal stopLoss,

		BigDecimal target1,

		BigDecimal target2,

		String grade,

		ScoreBreakdown scoreBreakdown,

		List<String> reasons

) {

	/**
	 * Returns true when this contract is considered tradable.
	 */
	public boolean tradable() {
		return score >= 70.0;
	}

	/**
	 * Returns true when confidence is high.
	 */
	public boolean highConfidence() {
		return confidence >= 85.0;
	}

	/**
	 * Returns a compact display name.
	 */
	public String displayName() {
		return tradingSymbol + " (" + strikePrice + " " + optionType + ")";
	}

	/**
	 * Returns primary reason if available.
	 */
	public String primaryReason() {
		return (reasons == null || reasons.isEmpty()) ? "" : reasons.get(0);
	}

	/**
	 * Returns Risk : Reward ratio.
	 */
	public BigDecimal riskReward() {

		if (entry == null || stopLoss == null || target1 == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal risk = entry.subtract(stopLoss).abs();

		if (risk.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal reward = target1.subtract(entry).abs();

		return reward.divide(risk, 2, java.math.RoundingMode.HALF_UP);
	}

	/**
	 * Returns recommendation label.
	 */
	public String recommendation() {

		if (!tradable()) {
			return "REJECT";
		}

		if (score >= 90) {
			return "STRONG BUY";
		}

		if (score >= 80) {
			return "BUY";
		}

		return "WATCH";
	}
}