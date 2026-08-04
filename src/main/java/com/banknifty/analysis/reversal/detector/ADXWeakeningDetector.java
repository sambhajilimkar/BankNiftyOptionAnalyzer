package com.banknifty.analysis.reversal.detector;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.analysis.reversal.ReversalDetector;
import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.ReversalReason;
import com.banknifty.analysis.reversal.ReversalResult;
import com.banknifty.indicator.result.ADXResult;

@Component
public class ADXWeakeningDetector implements ReversalDetector {

	private static final double WEAK_TREND = 20.0;
	private static final double MODERATE_TREND = 25.0;
	private static final double STRONG_TREND = 35.0;

	@Override
	public void detect(ReversalContext context, ReversalResult result) {

		if (context == null || context.getIndicators() == null || context.getIndicators().adx() == null) {
			return;
		}

		ADXResult adx = context.getIndicators().adx();

		double adxValue = adx.adx();

		/*
		 * ---------------------------------------------------- Very weak trend High
		 * probability of reversal / range
		 * ----------------------------------------------------
		 */
		if (adxValue < WEAK_TREND) {

			result.addScore(15);

			if (adx.bullish()) {

				result.setDirection(ReversalDirection.BEARISH);

			} else if (adx.bearish()) {

				result.setDirection(ReversalDirection.BULLISH);
			}

			result.addReason(ReversalReason.ADX_WEAKENING);

			return;
		}

		/*
		 * ---------------------------------------------------- Moderate trend Trend
		 * losing strength ----------------------------------------------------
		 */
		if (adxValue < MODERATE_TREND) {

			result.addScore(10);

			if (adx.bullish()) {

				result.setDirection(ReversalDirection.BEARISH);

			} else if (adx.bearish()) {

				result.setDirection(ReversalDirection.BULLISH);
			}

			result.addReason(ReversalReason.ADX_WEAKENING);

			return;
		}

		/*
		 * ---------------------------------------------------- Strong trend Small
		 * warning only ----------------------------------------------------
		 */
		if (adxValue < STRONG_TREND) {

			result.addScore(3);
		}
	}
}