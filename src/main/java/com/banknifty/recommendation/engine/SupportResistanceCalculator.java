package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class SupportResistanceCalculator {

	/**
	 * Professional Option Chain Support / Resistance
	 *
	 * Support -------- Highest Put OI
	 *
	 * Resistance ---------- Highest Call OI
	 *
	 * Strong Support -------------- Highest Put OI + Highest Put Volume
	 *
	 * Strong Resistance ----------------- Highest Call OI + Highest Call Volume
	 */

	public void calculate(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot == null) {
			return;
		}

		List<OptionStrike> calls = snapshot.calls();
		List<OptionStrike> puts = snapshot.puts();

		if (calls == null || puts == null) {
			return;
		}

		OptionStrike callWall = strongestCallWall(calls);
		OptionStrike putWall = strongestPutWall(puts);

		if (callWall != null) {

			analysis.setStrongestResistanceStrike(callWall.strike());
		}

		if (putWall != null) {

			analysis.setStrongestSupportStrike(putWall.strike());
		}

		score(snapshot, analysis, callWall, putWall);
	}

	/**
	 * Highest Institutional Call Wall
	 */
	private OptionStrike strongestCallWall(List<OptionStrike> calls) {

		return calls.stream()

				.filter(c -> c.openInterest() != null)

				.max(Comparator.comparingDouble(this::strength))

				.orElse(null);
	}

	/**
	 * Highest Institutional Put Wall
	 */
	private OptionStrike strongestPutWall(List<OptionStrike> puts) {

		return puts.stream()

				.filter(p -> p.openInterest() != null)

				.max(Comparator.comparingDouble(this::strength))

				.orElse(null);
	}

	/**
	 * Institutional Wall Strength
	 *
	 * Weight
	 *
	 * OI 60% Volume 30% IV 10%
	 */
	private double strength(OptionStrike strike) {

		double oi = strike.openInterest() == null ? 0 : strike.openInterest();

		double volume = strike.volume() == null ? 0 : strike.volume();

		double iv = strike.iv() == null ? 0 : strike.iv().doubleValue();

		return (oi * 0.60) + (volume * 0.30) + (iv * 0.10);
	}

	/**
	 * Score
	 */
	private void score(OptionSnapshot snapshot, InstitutionalAnalysis analysis, OptionStrike resistance,
			OptionStrike support) {

		if (snapshot.atmStrike() == null || resistance == null || support == null) {

			analysis.setSupportResistanceScore(0);

			return;
		}

		int atm = snapshot.atmStrike();

		int supportDistance = Math.abs(atm - support.strike());

		int resistanceDistance = Math.abs(resistance.strike() - atm);

		int corridor = supportDistance + resistanceDistance;

		double score;

		/*
		 * Very Tight Institutional Range
		 */
		if (corridor <= 200) {

			score = 100;

		} else if (corridor <= 400) {

			score = 95;

		} else if (corridor <= 600) {

			score = 90;

		} else if (corridor <= 800) {

			score = 80;

		} else if (corridor <= 1000) {

			score = 70;

		} else {

			score = 50;
		}

		analysis.setSupportResistanceScore(score);
	}

	/**
	 * Distance from nearest wall.
	 */
	public BigDecimal distanceFromSupport(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot.spotPrice() == null || analysis.getStrongestSupportStrike() == null) {

			return BigDecimal.ZERO;
		}

		return snapshot.spotPrice().subtract(BigDecimal.valueOf(analysis.getStrongestSupportStrike()));
	}

	public BigDecimal distanceFromResistance(OptionSnapshot snapshot, InstitutionalAnalysis analysis) {

		if (snapshot.spotPrice() == null || analysis.getStrongestResistanceStrike() == null) {

			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(analysis.getStrongestResistanceStrike()).subtract(snapshot.spotPrice());
	}

}