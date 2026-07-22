package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

@Component
public class OIBuildupCalculator {

	/**
	 * Compare previous snapshot with current snapshot.
	 *
	 * Build-up Rules
	 *
	 * Price↑ OI↑ = Long Build-up Price↓ OI↑ = Short Build-up Price↓ OI↓ = Long
	 * Unwinding Price↑ OI↓ = Short Covering
	 */
	public void calculate(OptionSnapshot previous, OptionSnapshot current, InstitutionalAnalysis analysis) {

		if (previous == null || current == null) {
			return;
		}

		Map<String, OptionStrike> previousMap = toMap(previous);
		Map<String, OptionStrike> currentMap = toMap(current);

		int longBuildUp = 0;
		int shortBuildUp = 0;
		int longUnwinding = 0;
		int shortCovering = 0;

		for (Map.Entry<String, OptionStrike> entry : currentMap.entrySet()) {

			OptionStrike currentStrike = entry.getValue();
			OptionStrike previousStrike = previousMap.get(entry.getKey());

			if (previousStrike == null) {
				continue;
			}

			if (currentStrike.ltp() == null || previousStrike.ltp() == null || currentStrike.openInterest() == null
					|| previousStrike.openInterest() == null) {
				continue;
			}

			double priceDiff = currentStrike.ltp().subtract(previousStrike.ltp()).doubleValue();

			long oiDiff = currentStrike.openInterest() - previousStrike.openInterest();

			if (priceDiff > 0 && oiDiff > 0) {

				longBuildUp++;

			} else if (priceDiff < 0 && oiDiff > 0) {

				shortBuildUp++;

			} else if (priceDiff < 0 && oiDiff < 0) {

				longUnwinding++;

			} else if (priceDiff > 0 && oiDiff < 0) {

				shortCovering++;
			}
		}

		int total = longBuildUp + shortBuildUp + longUnwinding + shortCovering;

		if (total == 0) {
			return;
		}

		analysis.setLongBuildUpScore(percentage(longBuildUp, total));

		analysis.setShortBuildUpScore(percentage(shortBuildUp, total));

		analysis.setLongUnwindingScore(percentage(longUnwinding, total));

		analysis.setShortCoveringScore(percentage(shortCovering, total));

		/*
		 * Institutional OI Score
		 */

		double score = Math.max(

				Math.max(analysis.getLongBuildUpScore(), analysis.getShortBuildUpScore()),

				Math.max(analysis.getLongUnwindingScore(), analysis.getShortCoveringScore()));

		analysis.setOiScore(score);
	}

	private Map<String, OptionStrike> toMap(OptionSnapshot snapshot) {

		Map<String, OptionStrike> map = new HashMap<>();

		snapshot.calls().forEach(s -> map.put(key(s), s));

		snapshot.puts().forEach(s -> map.put(key(s), s));

		return map;
	}

	private String key(OptionStrike strike) {

		return strike.expiry() + "-" + strike.strike() + "-" + strike.optionType();
	}

	private double percentage(int value, int total) {

		return BigDecimal.valueOf(value * 100.0).divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}

}