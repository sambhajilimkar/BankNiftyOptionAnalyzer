package com.banknifty.analysis.reversal.detector;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.analysis.reversal.ReversalDetector;
import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.ReversalReason;
import com.banknifty.analysis.reversal.ReversalResult;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.indicator.result.VWAPResult;

@Component
public class VWAPFailureDetector implements ReversalDetector {

	/**
	 * Price almost touching VWAP.
	 */
	private static final BigDecimal NEAR_VWAP = BigDecimal.valueOf(0.30);

	/**
	 * Price clearly away from VWAP.
	 */
	private static final BigDecimal STRONG_DISTANCE = BigDecimal.valueOf(1.00);

	@Override
	public void detect(ReversalContext context, ReversalResult result) {

		if (context == null || context.getIndicators() == null) {
			return;
		}

		IndicatorSnapshot snapshot = context.getIndicators();

		VWAPResult vwap = snapshot.vwap();

		if (vwap == null || vwap.distance() == null) {
			return;
		}

		BigDecimal distance = vwap.distance().abs();

		/*
		 * ---------------------------------------------------- Failed breakout near
		 * VWAP ----------------------------------------------------
		 */
		if (vwap.breakout() && distance.compareTo(NEAR_VWAP) <= 0) {

			result.addScore(15);

			if (vwap.aboveVWAP()) {

				result.setDirection(ReversalDirection.BEARISH);

			} else {

				result.setDirection(ReversalDirection.BULLISH);
			}

			result.addReason(ReversalReason.VWAP_BREAKDOWN);

			return;
		}

		/*
		 * ---------------------------------------------------- Healthy pullback
		 * ----------------------------------------------------
		 */
		if (vwap.pullback()) {

			result.addScore(8);

			if (vwap.aboveVWAP()) {

				result.setDirection(ReversalDirection.BULLISH);

				result.addReason(ReversalReason.VWAP_BREAKOUT);

			} else {

				result.setDirection(ReversalDirection.BEARISH);

				result.addReason(ReversalReason.VWAP_BREAKDOWN);
			}

			return;
		}

		/*
		 * ---------------------------------------------------- Trend extended far away
		 * from VWAP. Increased probability of mean reversion.
		 * ----------------------------------------------------
		 */
		if (distance.compareTo(STRONG_DISTANCE) >= 0) {

			result.addScore(5);

			if (vwap.aboveVWAP()) {

				result.setDirection(ReversalDirection.BEARISH);

			} else {

				result.setDirection(ReversalDirection.BULLISH);
			}
		}
	}
}