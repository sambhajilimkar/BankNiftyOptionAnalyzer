package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class MaxPainCalculator {

	/**
	 * NSE Max Pain
	 *
	 * For every possible expiry settlement strike:
	 *
	 * Call Writer Loss + Put Writer Loss
	 *
	 * = Total Pain
	 *
	 * Strike having MINIMUM total pain = Max Pain
	 */
	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot == null) {
			return;
		}

		List<Integer> strikes = collectStrikes(snapshot);

		if (strikes.isEmpty()) {
			return;
		}

		long minimumPain = Long.MAX_VALUE;
		Integer maxPainStrike = null;

		for (Integer expiryPrice : strikes) {

			long totalPain = calculateTotalPain(snapshot, expiryPrice);

			if (totalPain < minimumPain) {

				minimumPain = totalPain;
				maxPainStrike = expiryPrice;
			}
		}

		analysis.setMaxPainStrike(maxPainStrike);

		score(snapshot, analysis, maxPainStrike);
	}

	/**
	 * Total Writer Pain
	 */
	private long calculateTotalPain(OptionSnapshot snapshot, Integer expiryPrice) {

		long callPain = 0L;
		long putPain = 0L;

		/*
		 * CALL WRITERS
		 */
		for (OptionStrike call : snapshot.calls()) {

			if (call.openInterest() == null) {
				continue;
			}

			int intrinsic = Math.max(0, expiryPrice - call.strike());

			callPain += (long) intrinsic * call.openInterest();
		}

		/*
		 * PUT WRITERS
		 */
		for (OptionStrike put : snapshot.puts()) {

			if (put.openInterest() == null) {
				continue;
			}

			int intrinsic = Math.max(0, put.strike() - expiryPrice);

			putPain += (long) intrinsic * put.openInterest();
		}

		return callPain + putPain;
	}

	/**
	 * Score based on current ATM distance.
	 */
	private void score(OptionSnapshot snapshot, InstitutionalAnalysis analysis, Integer maxPainStrike) {

		if (snapshot.atmStrike() == null || maxPainStrike == null) {

			analysis.setMaxPainScore(0);
			return;
		}

		int distance = Math.abs(snapshot.atmStrike() - maxPainStrike);

		double score;

		if (distance <= 100) {

			score = 100;

		} else if (distance <= 200) {

			score = 95;

		} else if (distance <= 300) {

			score = 90;

		} else if (distance <= 500) {

			score = 80;

		} else if (distance <= 700) {

			score = 70;

		} else if (distance <= 1000) {

			score = 60;

		} else {

			score = 40;
		}

		analysis.setMaxPainScore(score);
	}

	private List<Integer> collectStrikes(OptionSnapshot snapshot) {

		List<Integer> strikes = new ArrayList<>();

		for (OptionStrike strike : snapshot.calls()) {

			if (strike.strike() != null) {

				strikes.add(strike.strike());
			}
		}

		return strikes.stream().distinct().sorted().toList();
	}

}