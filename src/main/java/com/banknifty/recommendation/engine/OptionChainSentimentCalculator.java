package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

import com.banknifty.analysis.MarketBias;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class OptionChainSentimentCalculator {

	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot == null || snapshot.calls() == null || snapshot.puts() == null
				|| (snapshot.calls().isEmpty() && snapshot.puts().isEmpty())) {
			return;
		}

		long totalCallOI = analysis.getTotalCallOI();
		long totalPutOI = analysis.getTotalPutOI();

		long totalCallVolume = analysis.getTotalCallVolume();
		long totalPutVolume = analysis.getTotalPutVolume();

		double bullish = 0;
		double bearish = 0;

		/*
		 * ============================================================ PCR Contribution
		 * ============================================================
		 */

		if (analysis.getPutCallRatio() != null) {

			double pcr = analysis.getPutCallRatio().doubleValue();

			if (pcr >= 1.20) {

				bullish += 25;

			} else if (pcr <= 0.80) {

				bearish += 25;

			} else {

				bullish += 10;
				bearish += 10;
			}
		}

		/*
		 * ============================================================ OI Contribution
		 * ============================================================
		 */

		if (totalPutOI > totalCallOI) {

			bullish += 20;

		} else if (totalCallOI > totalPutOI) {

			bearish += 20;
		}

		/*
		 * ============================================================ Volume
		 * Contribution ============================================================
		 */

		if (totalPutVolume > totalCallVolume) {

			bullish += 15;

		} else if (totalCallVolume > totalPutVolume) {

			bearish += 15;
		}

		/*
		 * ============================================================ Max Pain
		 * Contribution ============================================================
		 */

		if (analysis.getMaxPainStrike() != null) {

			bullish += analysis.getMaxPainScore() * 0.10;
			bearish += analysis.getMaxPainScore() * 0.10;
		}

		/*
		 * ============================================================ Gamma
		 * Contribution ============================================================
		 */

		bullish += analysis.getGammaExposureScore() * 0.15;
		bearish += analysis.getGammaExposureScore() * 0.15;

		/*
		 * ============================================================ Liquidity
		 * Contribution ============================================================
		 */

		bullish += analysis.getLiquidityScore() * 0.10;
		bearish += analysis.getLiquidityScore() * 0.10;

		/*
		 * ============================================================ Final Bias
		 * ============================================================
		 */

		MarketBias bias;

		if (bullish > bearish + 10) {

			bias = MarketBias.BULLISH;

		} else if (bearish > bullish + 10) {

			bias = MarketBias.BEARISH;

		} else {

			bias = MarketBias.SIDEWAYS;
		}

		analysis.setMarketBias(bias);

		/*
		 * ============================================================ Confidence
		 * ============================================================
		 */

		double confidence;

		if (bullish == 0 && bearish == 0) {

			confidence = 0;

		} else {

			confidence = (Math.max(bullish, bearish) * 100.0) / (bullish + bearish);
		}

		analysis.setConfidence(Math.min(confidence, 100));

		/*
		 * ============================================================ IV Percentile
		 * (Initial Approximation)
		 * ============================================================
		 */

		if (analysis.getAverageIV() != null) {

			BigDecimal iv = analysis.getAverageIV();

			BigDecimal percentile = iv.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100));

			analysis.setIvPercentile(percentile.min(BigDecimal.valueOf(100)));
		}

		/*
		 * ============================================================ Institutional
		 * Composite Score ============================================================
		 */

		double score = (analysis.getPcrScore() + analysis.getOiScore() + analysis.getMaxPainScore()
				+ analysis.getGammaExposureScore() + analysis.getLiquidityScore() + analysis.getVolatilityScore()
				+ analysis.getSupportResistanceScore()) / 7.0;

		analysis.setInstitutionalScore(score);
	}

}
