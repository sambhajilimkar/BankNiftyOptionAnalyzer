package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.banknifty.analysis.MarketBias;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class PCRCalculator {

	/**
	 * Number of strikes above/below ATM to include in Near-ATM PCR.
	 */
	@Value("${analysis.pcr.near-atm-strikes:5}")
	private int nearAtmStrikes;

	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot == null) {
			return;
		}

		List<OptionStrike> calls = snapshot.calls();
		List<OptionStrike> puts = snapshot.puts();

		if (calls == null || puts == null) {
			return;
		}

		/*
		 * ============================================================ Overall OI
		 * ============================================================
		 */

		long totalCallOI = calls.stream().map(OptionStrike::openInterest).filter(v -> v != null)
				.mapToLong(Long::longValue).sum();

		long totalPutOI = puts.stream().map(OptionStrike::openInterest).filter(v -> v != null)
				.mapToLong(Long::longValue).sum();

		analysis.setTotalCallOI(totalCallOI);
		analysis.setTotalPutOI(totalPutOI);

		/*
		 * ============================================================ Overall Volume
		 * ============================================================
		 */

		long totalCallVolume = calls.stream().map(OptionStrike::volume).filter(v -> v != null)
				.mapToLong(Long::longValue).sum();

		long totalPutVolume = puts.stream().map(OptionStrike::volume).filter(v -> v != null).mapToLong(Long::longValue)
				.sum();

		analysis.setTotalCallVolume(totalCallVolume);
		analysis.setTotalPutVolume(totalPutVolume);

		/*
		 * ============================================================ Overall PCR (OI)
		 * ============================================================
		 */

		BigDecimal overallPCR = divide(totalPutOI, totalCallOI);

		analysis.setPutCallRatio(overallPCR);

		/*
		 * ============================================================ Volume PCR
		 * ============================================================
		 */

		BigDecimal volumePCR = divide(totalPutVolume, totalCallVolume);

		/*
		 * ============================================================ ATM PCR
		 * ============================================================
		 */

		Integer atm = snapshot.atmStrike();

		BigDecimal atmPCR = BigDecimal.ZERO;

		if (atm != null) {

			Long callOI = calls.stream().filter(c -> atm.equals(c.strike())).map(OptionStrike::openInterest).findFirst()
					.orElse(0L);

			Long putOI = puts.stream().filter(p -> atm.equals(p.strike())).map(OptionStrike::openInterest).findFirst()
					.orElse(0L);

			atmPCR = divide(putOI, callOI);
		}

		/*
		 * ============================================================ Near ATM PCR
		 * ============================================================
		 */

		BigDecimal nearAtmPCR = calculateNearAtmPCR(calls, puts, atm);

		/*
		 * ============================================================ Weighted
		 * Institutional PCR
		 *
		 * 40% Overall 30% Near ATM 20% ATM 10% Volume
		 * ============================================================
		 */

		BigDecimal weightedPCR = overallPCR.multiply(BigDecimal.valueOf(0.40))
				.add(nearAtmPCR.multiply(BigDecimal.valueOf(0.30))).add(atmPCR.multiply(BigDecimal.valueOf(0.20)))
				.add(volumePCR.multiply(BigDecimal.valueOf(0.10)));

		/*
		 * ============================================================ Institutional
		 * Score ============================================================
		 */

		double score = score(weightedPCR.doubleValue());

		analysis.setPcrScore(score);

		if (weightedPCR.doubleValue() >= 1.15) {

			analysis.setMarketBias(MarketBias.BULLISH);

		} else if (weightedPCR.doubleValue() <= 0.85) {

			analysis.setMarketBias(MarketBias.BEARISH);

		} else {

			analysis.setMarketBias(MarketBias.SIDEWAYS);
		}
	}

	private BigDecimal calculateNearAtmPCR(List<OptionStrike> calls, List<OptionStrike> puts, Integer atmStrike) {

		if (atmStrike == null) {
			return BigDecimal.ONE;
		}

		List<Integer> strikes = calls.stream().map(OptionStrike::strike).sorted().collect(Collectors.toList());

		int atmIndex = strikes.indexOf(atmStrike);

		if (atmIndex < 0) {
			return BigDecimal.ONE;
		}

		int start = Math.max(0, atmIndex - nearAtmStrikes);
		int end = Math.min(strikes.size() - 1, atmIndex + nearAtmStrikes);

		long callOI = 0;
		long putOI = 0;

		for (int i = start; i <= end; i++) {

			Integer strike = strikes.get(i);

			callOI += calls.stream().filter(c -> strike.equals(c.strike())).map(OptionStrike::openInterest)
					.filter(v -> v != null).findFirst().orElse(0L);

			putOI += puts.stream().filter(p -> strike.equals(p.strike())).map(OptionStrike::openInterest)
					.filter(v -> v != null).findFirst().orElse(0L);
		}

		return divide(putOI, callOI);
	}

	private BigDecimal divide(long numerator, long denominator) {

		if (denominator <= 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
	}

	private double score(double pcr) {

		if (pcr >= 1.40)
			return 100;

		if (pcr >= 1.25)
			return 95;

		if (pcr >= 1.15)
			return 90;

		if (pcr >= 1.05)
			return 80;

		if (pcr >= 0.95)
			return 70;

		if (pcr >= 0.85)
			return 60;

		if (pcr >= 0.75)
			return 45;

		return 30;
	}

}