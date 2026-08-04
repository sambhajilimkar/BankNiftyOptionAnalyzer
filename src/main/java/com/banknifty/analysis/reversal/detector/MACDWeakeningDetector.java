package com.banknifty.analysis.reversal.detector;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.analysis.reversal.ReversalDetector;
import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.ReversalReason;
import com.banknifty.analysis.reversal.ReversalResult;
import com.banknifty.indicator.result.MACDResult;

@Component
public class MACDWeakeningDetector implements ReversalDetector {

	private static final BigDecimal HISTOGRAM_WEAK_ZONE = BigDecimal.valueOf(5);

	private static final BigDecimal HISTOGRAM_STRONG_ZONE = BigDecimal.valueOf(15);

	@Override
	public void detect(ReversalContext context, ReversalResult result) {

		if (context == null || context.getIndicators() == null || context.getIndicators().macd() == null) {
			return;
		}

		MACDResult macd = context.getIndicators().macd();

		BigDecimal histogram = macd.histogram();

		if (histogram == null) {
			return;
		}

		BigDecimal absHistogram = histogram.abs();

		/*
		 * ------------------------------------------------------- Strong crossover
		 * -------------------------------------------------------
		 */

		if (macd.bearishCross()) {

			result.addScore(18);

			result.setDirection(ReversalDirection.BEARISH);

			result.addReason(ReversalReason.MACD_BEARISH_DIVERGENCE);

			return;
		}

		if (macd.bullishCross()) {

			result.addScore(18);

			result.setDirection(ReversalDirection.BULLISH);

			result.addReason(ReversalReason.MACD_BULLISH_DIVERGENCE);

			return;
		}

		/*
		 * ------------------------------------------------------- Histogram almost flat
		 * Momentum exhaustion -------------------------------------------------------
		 */

		if (absHistogram.compareTo(HISTOGRAM_WEAK_ZONE) <= 0) {

			result.addScore(10);

			if (macd.bullish()) {

				result.setDirection(ReversalDirection.BEARISH);

				result.addReason(ReversalReason.MACD_BEARISH_DIVERGENCE);

			} else if (macd.bearish()) {

				result.setDirection(ReversalDirection.BULLISH);

				result.addReason(ReversalReason.MACD_BULLISH_DIVERGENCE);
			}

			return;
		}

		/*
		 * ------------------------------------------------------- Histogram shrinking
		 * but trend still alive Give a smaller warning score.
		 * -------------------------------------------------------
		 */

		if (absHistogram.compareTo(HISTOGRAM_STRONG_ZONE) <= 0) {

			result.addScore(5);

			if (macd.bullish()) {

				result.setDirection(ReversalDirection.BEARISH);

			} else if (macd.bearish()) {

				result.setDirection(ReversalDirection.BULLISH);
			}
		}
	}
}