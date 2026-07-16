package com.banknifty.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionAnalysis {

	/*
	 * ============================================================ Option Contract
	 * ============================================================
	 */

	private OptionCandidate candidate;

	/*
	 * ============================================================ Individual
	 * Scores (0-100) ============================================================
	 */

	private double trendScore;

	private double strikeScore;

	private double liquidityScore;

	private double openInterestScore;

	private double supportResistanceScore;

	private double pivotScore;

	private double expiryScore;

	private double volatilityScore;

	private double greekScore;

	private double probabilityScore;

	private double expectedMoveScore;

	private double riskRewardScore;

	/*
	 * ============================================================ Final Result
	 * ============================================================
	 */

	private double totalScore;

	private double confidence;

	private int rank;

	/*
	 * ============================================================ Suggested Trade
	 * ============================================================
	 */

	private BigDecimal entry;

	private BigDecimal stopLoss;

	private BigDecimal target1;

	private BigDecimal target2;

	/*
	 * ============================================================ Analysis
	 * ============================================================
	 */

	@Builder.Default
	private List<String> reasons = new ArrayList<>();

	/*
	 * ============================================================ Helper Methods
	 * ============================================================
	 */

	public void addReason(String reason) {

		reasons.add(reason);

	}

	public void addScore(double score) {

		totalScore += score;

	}

	public void calculateConfidence() {

		confidence = Math.min(totalScore, 100.0);

	}

}