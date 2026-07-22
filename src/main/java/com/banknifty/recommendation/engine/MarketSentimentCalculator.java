package com.banknifty.recommendation.engine;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.MarketBias;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class MarketSentimentCalculator {

	/*
	 * ============================================================ Institutional
	 * Weights ============================================================
	 */

	private static final double PCR_WEIGHT = 0.20;

	private static final double OI_WEIGHT = 0.20;

	private static final double MAX_PAIN_WEIGHT = 0.15;

	private static final double GAMMA_WEIGHT = 0.15;

	private static final double SUPPORT_RESISTANCE_WEIGHT = 0.15;

	private static final double VOLATILITY_WEIGHT = 0.10;

	private static final double LIQUIDITY_WEIGHT = 0.05;

	public void calculate(InstitutionalAnalysis analysis) {

		if (analysis == null) {
			return;
		}

		double score = calculateInstitutionalScore(analysis);

		analysis.setInstitutionalScore(score);

		analysis.setMarketBias(determineBias(score));

		analysis.setConfidence(calculateConfidence(score, analysis));
	}

	/**
	 * ============================================================ Composite
	 * Institutional Score
	 * ============================================================
	 */
	private double calculateInstitutionalScore(InstitutionalAnalysis analysis) {

		double score = 0;

		score += analysis.getPcrScore() * PCR_WEIGHT;

		score += analysis.getOiScore() * OI_WEIGHT;

		score += analysis.getMaxPainScore() * MAX_PAIN_WEIGHT;

		score += analysis.getGammaExposureScore() * GAMMA_WEIGHT;

		score += analysis.getSupportResistanceScore() * SUPPORT_RESISTANCE_WEIGHT;

		score += analysis.getVolatilityScore() * VOLATILITY_WEIGHT;

		score += analysis.getLiquidityScore() * LIQUIDITY_WEIGHT;

		return Math.min(score, 100.0);
	}

	/**
	 * ============================================================ Institutional
	 * Bias ============================================================
	 */
	private MarketBias determineBias(double score) {

		if (score >= 85) {
			return MarketBias.STRONG_BULLISH;
		}

		if (score >= 70) {
			return MarketBias.BULLISH;
		}

		if (score >= 55) {
			return MarketBias.SIDEWAYS;
		}

		if (score >= 40) {
			return MarketBias.BEARISH;
		}

		return MarketBias.STRONG_BEARISH;
	}

	/**
	 * ============================================================ Confidence
	 * ============================================================
	 *
	 * Confidence is based on agreement among the institutional signals.
	 */
	private double calculateConfidence(double score, InstitutionalAnalysis analysis) {

		double[] values = { analysis.getPcrScore(), analysis.getOiScore(), analysis.getMaxPainScore(),
				analysis.getGammaExposureScore(), analysis.getSupportResistanceScore(), analysis.getVolatilityScore(),
				analysis.getLiquidityScore() };

		double deviation = 0;

		for (double value : values) {
			deviation += Math.abs(score - value);
		}

		deviation = deviation / values.length;

		double confidence = 100 - deviation;

		if (confidence < 0) {
			confidence = 0;
		}

		if (confidence > 100) {
			confidence = 100;
		}

		return confidence;
	}

}