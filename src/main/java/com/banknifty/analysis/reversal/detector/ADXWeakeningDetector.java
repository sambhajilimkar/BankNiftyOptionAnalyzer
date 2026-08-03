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

	private static final double STRONG_TREND = 25.0;

	@Override
	public void detect(ReversalContext context, ReversalResult result) {

		ADXResult adx = context.getIndicators().adx();

		if (adx == null) {
			return;
		}

		double adxValue = adx.adx();

		/*
		 * ADX below 25 means the trend is losing strength.
		 */
		if (adxValue < STRONG_TREND) {

			result.addScore(10);

			if (adx.bullish()) {
				result.setDirection(ReversalDirection.BEARISH);
			} else if (adx.bearish()) {
				result.setDirection(ReversalDirection.BULLISH);
			}

			result.addReason(ReversalReason.ADX_WEAKENING);
		}
	}
}