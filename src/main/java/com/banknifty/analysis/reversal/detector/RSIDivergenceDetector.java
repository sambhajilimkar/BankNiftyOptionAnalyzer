package com.banknifty.analysis.reversal.detector;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.analysis.reversal.ReversalDetector;
import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.ReversalReason;
import com.banknifty.analysis.reversal.ReversalResult;
import com.banknifty.indicator.result.RSIResult;
import com.banknifty.model.Candle;

@Component
public class RSIDivergenceDetector implements ReversalDetector {

	private static final int LOOKBACK = 15;

	private static final BigDecimal MIN_PRICE_MOVE = BigDecimal.valueOf(20);

	private static final BigDecimal MIN_RSI_MOVE = BigDecimal.valueOf(2);

	@Override
	public void detect(ReversalContext context, ReversalResult result) {

		if (context == null || context.getIndicators() == null || context.getIndicators().rsi() == null
				|| context.getCandles() == null) {
			return;
		}

		List<Candle> candles = context.getCandles();

		if (candles.size() < LOOKBACK) {
			return;
		}

		RSIResult rsi = context.getIndicators().rsi();

		BigDecimal currentLow = candles.get(candles.size() - 1).low();

		BigDecimal previousLow = candles.get(candles.size() - 6).low();

		BigDecimal currentHigh = candles.get(candles.size() - 1).high();

		BigDecimal previousHigh = candles.get(candles.size() - 6).high();

		BigDecimal currentRsi = rsi.rsi();
		BigDecimal previousRsi = rsi.previousRsi();

		if (currentRsi == null || previousRsi == null) {
			return;
		}

		/*
		 * ------------------------------------------------- Bullish Divergence
		 * -------------------------------------------------
		 */

		BigDecimal priceDrop = previousLow.subtract(currentLow);

		BigDecimal rsiRise = currentRsi.subtract(previousRsi);

		if (priceDrop.compareTo(MIN_PRICE_MOVE) >= 0 && rsiRise.compareTo(MIN_RSI_MOVE) >= 0) {

			result.addScore(15);

			result.setDirection(ReversalDirection.BULLISH);

			result.addReason(ReversalReason.RSI_BULLISH_DIVERGENCE);

			return;
		}

		/*
		 * ------------------------------------------------- Bearish Divergence
		 * -------------------------------------------------
		 */

		BigDecimal priceRise = currentHigh.subtract(previousHigh);

		BigDecimal rsiFall = previousRsi.subtract(currentRsi);

		if (priceRise.compareTo(MIN_PRICE_MOVE) >= 0 && rsiFall.compareTo(MIN_RSI_MOVE) >= 0) {

			result.addScore(15);

			result.setDirection(ReversalDirection.BEARISH);

			result.addReason(ReversalReason.RSI_BEARISH_DIVERGENCE);
		}
	}
}