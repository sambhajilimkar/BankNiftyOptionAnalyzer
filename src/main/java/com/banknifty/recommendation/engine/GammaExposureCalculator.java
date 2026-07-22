package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class GammaExposureCalculator {

	/**
	 * BankNifty lot size. Externalize in application.yml.
	 */
	@Value("${trading.banknifty.lot-size:35}")
	private int lotSize;

	private static final MathContext MC = new MathContext(12);

	/**
	 * Professional Gamma Exposure
	 *
	 * GEX = Gamma × Open Interest × Lot Size × Spot² × 0.01
	 */
	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot == null || snapshot.spotPrice() == null) {
			return;
		}

		BigDecimal callExposure = BigDecimal.ZERO;
		BigDecimal putExposure = BigDecimal.ZERO;

		BigDecimal totalDelta = BigDecimal.ZERO;
		BigDecimal totalGamma = BigDecimal.ZERO;
		BigDecimal totalTheta = BigDecimal.ZERO;
		BigDecimal totalVega = BigDecimal.ZERO;

		BigDecimal totalIV = BigDecimal.ZERO;

		int ivCount = 0;
		int gammaCount = 0;

		BigDecimal spotSquared = snapshot.spotPrice().multiply(snapshot.spotPrice(), MC);

		/*
		 * ============================================================ CALLS
		 * ============================================================
		 */

		for (OptionStrike strike : snapshot.calls()) {

			if (strike.gamma() == null || strike.openInterest() == null) {
				continue;
			}

			BigDecimal exposure = strike.gamma()

					.multiply(BigDecimal.valueOf(strike.openInterest()), MC)

					.multiply(BigDecimal.valueOf(lotSize), MC)

					.multiply(spotSquared, MC)

					.multiply(BigDecimal.valueOf(0.01), MC);

			callExposure = callExposure.add(exposure);
			gammaCount++;

			if (strike.delta() != null)
				totalDelta = totalDelta.add(strike.delta());

			if (strike.theta() != null)
				totalTheta = totalTheta.add(strike.theta());

			if (strike.vega() != null)
				totalVega = totalVega.add(strike.vega());

			totalGamma = totalGamma.add(strike.gamma());

			if (strike.iv() != null) {

				totalIV = totalIV.add(strike.iv());

				ivCount++;
			}
		}

		/*
		 * ============================================================ PUTS
		 * ============================================================
		 */

		for (OptionStrike strike : snapshot.puts()) {

			if (strike.gamma() == null || strike.openInterest() == null) {
				continue;
			}

			BigDecimal exposure = strike.gamma()

					.multiply(BigDecimal.valueOf(strike.openInterest()), MC)

					.multiply(BigDecimal.valueOf(lotSize), MC)

					.multiply(spotSquared, MC)

					.multiply(BigDecimal.valueOf(0.01), MC);

			putExposure = putExposure.add(exposure);
			gammaCount++;

			if (strike.delta() != null)
				totalDelta = totalDelta.add(strike.delta());

			if (strike.theta() != null)
				totalTheta = totalTheta.add(strike.theta());

			if (strike.vega() != null)
				totalVega = totalVega.add(strike.vega());

			totalGamma = totalGamma.add(strike.gamma());

			if (strike.iv() != null) {

				totalIV = totalIV.add(strike.iv());

				ivCount++;
			}
		}

		analysis.setTotalGamma(totalGamma);
		analysis.setTotalDelta(totalDelta);
		analysis.setTotalTheta(totalTheta);
		analysis.setTotalVega(totalVega);

		if (ivCount > 0) {

			analysis.setAverageIV(totalIV.divide(BigDecimal.valueOf(ivCount), 4, RoundingMode.HALF_UP));
		}

		/*
		 * ============================================================ Net Gamma
		 * Exposure ============================================================
		 */

		if (gammaCount == 0) {
			analysis.setGammaExposureScore(0);
			return;
		}

		BigDecimal netExposure = callExposure.subtract(putExposure);

		/*
		 * ============================================================ Institutional
		 * Score ============================================================
		 */

		double exposureCrores = netExposure.abs().divide(BigDecimal.valueOf(10_000_000), 2, RoundingMode.HALF_UP)
				.doubleValue();

		double score;

		if (exposureCrores >= 5000) {

			score = 100;

		} else if (exposureCrores >= 3000) {

			score = 95;

		} else if (exposureCrores >= 2000) {

			score = 90;

		} else if (exposureCrores >= 1000) {

			score = 80;

		} else if (exposureCrores >= 500) {

			score = 70;

		} else if (exposureCrores >= 250) {

			score = 60;

		} else {

			score = 40;
		}

		analysis.setGammaExposureScore(score);
	}

}
