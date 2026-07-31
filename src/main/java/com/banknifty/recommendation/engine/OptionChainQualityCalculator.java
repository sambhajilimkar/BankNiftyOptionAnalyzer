package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

/**
 * Calculates execution quality from the contracts that are close enough to the
 * current price to be realistic trade candidates. Far OTM contracts are not
 * included because their thin quotes would distort the market-quality score.
 */
@Component
public class OptionChainQualityCalculator {

	private static final int NEAR_ATM_RANGE = 500;
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {
		if (snapshot == null || snapshot.atmStrike() == null) {
			return;
		}

		List<OptionStrike> nearAtm = java.util.stream.Stream.concat(snapshot.calls().stream(), snapshot.puts().stream())
				.filter(strike -> strike.strike() != null
						&& Math.abs(strike.strike() - snapshot.atmStrike()) <= NEAR_ATM_RANGE)
				.toList();
		if (nearAtm.isEmpty()) {
			return;
		}

		calculateLiquidity(nearAtm, analysis);
		calculateVolatility(nearAtm, analysis);
	}

	private void calculateLiquidity(List<OptionStrike> strikes, InstitutionalAnalysis analysis) {
		List<OptionStrike> quoted = strikes.stream().filter(this::hasUsableQuote).toList();
		if (quoted.isEmpty()) {
			analysis.setLiquidityScore(0);
			return;
		}

		BigDecimal averageSpread = quoted.stream().map(this::spreadPercentage).reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(quoted.size()), 4, RoundingMode.HALF_UP);
		long averageVolume = quoted.stream().map(OptionStrike::volume).filter(value -> value != null)
				.mapToLong(Long::longValue).sum() / quoted.size();
		long averageOpenInterest = quoted.stream().map(OptionStrike::openInterest).filter(value -> value != null)
				.mapToLong(Long::longValue).sum() / quoted.size();

		analysis.setAverageSpread(averageSpread);
		analysis.setAverageLiquidity(BigDecimal.valueOf(averageVolume + averageOpenInterest));

		double spreadScore = spreadScore(averageSpread.doubleValue());
		double quoteCoverageScore = quoted.size() * 20.0 / strikes.size();
		double volumeScore = Math.min(10, averageVolume * 10.0 / 10_000);
		double openInterestScore = Math.min(10, averageOpenInterest * 10.0 / 50_000);
		analysis.setLiquidityScore(Math.min(100, spreadScore + quoteCoverageScore + volumeScore + openInterestScore));
	}

	private void calculateVolatility(List<OptionStrike> strikes, InstitutionalAnalysis analysis) {
		List<BigDecimal> impliedVolatilities = strikes.stream().map(OptionStrike::iv)
				.filter(iv -> iv != null && iv.signum() > 0).toList();
		if (impliedVolatilities.isEmpty()) {
			analysis.setVolatilityScore(0);
			return;
		}

		BigDecimal averageIv = impliedVolatilities.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(impliedVolatilities.size()), 4, RoundingMode.HALF_UP);
		double iv = averageIv.doubleValue();
		analysis.setAverageIV(averageIv);
		// IV percentile requires a time series of IV observations. Do not label the
		// current IV as a percentile until that history is available.
		analysis.setIvPercentile(null);
		analysis.setVolatilityScore(volatilityScore(iv));
	}

	private boolean hasUsableQuote(OptionStrike strike) {
		return strike.ltp() != null && strike.ltp().signum() > 0 && strike.bid() != null && strike.ask() != null
				&& strike.bid().signum() > 0 && strike.ask().compareTo(strike.bid()) >= 0;
	}

	private BigDecimal spreadPercentage(OptionStrike strike) {
		return strike.ask().subtract(strike.bid()).multiply(ONE_HUNDRED).divide(strike.ltp(), 4,
				RoundingMode.HALF_UP);
	}

	private double spreadScore(double spreadPercentage) {
		if (spreadPercentage <= 0.5) return 60;
		if (spreadPercentage <= 1.0) return 50;
		if (spreadPercentage <= 2.0) return 40;
		if (spreadPercentage <= 3.0) return 25;
		if (spreadPercentage <= 5.0) return 10;
		return 0;
	}

	private double volatilityScore(double iv) {
		if (iv >= 10 && iv <= 25) return 100;
		if (iv >= 5 && iv < 10) return 70;
		if (iv > 25 && iv <= 35) return 75;
		if (iv > 35 && iv <= 50) return 45;
		return 20;
	}
}
