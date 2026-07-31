package com.banknifty.recommendation.engine;

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

		// Max pain, gamma and liquidity describe market quality/range conditions.
		// They are deliberately excluded from directional confidence: adding the
		// same value to both sides used to make a neutral chain look highly certain.

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

	}

}
