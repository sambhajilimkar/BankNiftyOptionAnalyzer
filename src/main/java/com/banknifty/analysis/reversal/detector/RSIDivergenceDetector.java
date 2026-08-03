package com.banknifty.analysis.reversal.detector;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.reversal.ReversalContext;
import com.banknifty.analysis.reversal.ReversalDetector;
import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.ReversalReason;
import com.banknifty.analysis.reversal.ReversalResult;
import com.banknifty.model.Candle;

@Component
public class RSIDivergenceDetector implements ReversalDetector {

    private static final int LOOKBACK = 15;

    @Override
    public void detect(ReversalContext context,
                       ReversalResult result) {

        List<Candle> candles = context.getCandles();

        if (candles == null || candles.size() < LOOKBACK) {
            return;
        }

        BigDecimal currentLow = candles.get(candles.size() - 1).low();
        BigDecimal previousLow = candles.get(candles.size() - 6).low();

        BigDecimal currentHigh = candles.get(candles.size() - 1).high();
        BigDecimal previousHigh = candles.get(candles.size() - 6).high();

        BigDecimal currentRsi = context.getIndicators().rsi().rsi();
        BigDecimal previousRsi = context.getIndicators().rsi().previousRsi();

        /*
         * Bullish Divergence
         *
         * Price makes Lower Low
         * RSI makes Higher Low
         */

        if (currentLow.compareTo(previousLow) < 0
                && currentRsi.compareTo(previousRsi) > 0) {

            result.addScore(15);

            result.setDirection(ReversalDirection.BULLISH);

            result.addReason(ReversalReason.RSI_BULLISH_DIVERGENCE);

            return;
        }

        /*
         * Bearish Divergence
         *
         * Price makes Higher High
         * RSI makes Lower High
         */

        if (currentHigh.compareTo(previousHigh) > 0
                && currentRsi.compareTo(previousRsi) < 0) {

            result.addScore(15);

            result.setDirection(ReversalDirection.BEARISH);

            result.addReason(ReversalReason.RSI_BEARISH_DIVERGENCE);
        }
    }

}