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

    @Override
    public void detect(ReversalContext context,
                       ReversalResult result) {

        MACDResult macd = context.getIndicators().macd();

        if (macd == null) {
            return;
        }

        BigDecimal histogram = macd.histogram();

        if (histogram == null) {
            return;
        }

        /*
         * Histogram close to zero means
         * momentum is fading.
         */

        if (histogram.abs().doubleValue() < 5.0) {

            if (macd.bullish()) {

                result.addScore(8);

                result.setDirection(ReversalDirection.BEARISH);

                result.addReason(
                        ReversalReason.MACD_BEARISH_DIVERGENCE);

                return;
            }

            if (macd.bearish()) {

                result.addScore(8);

                result.setDirection(ReversalDirection.BULLISH);

                result.addReason(
                        ReversalReason.MACD_BULLISH_DIVERGENCE);
            }
        }

        /*
         * Crossovers are stronger reversal signals.
         */

        if (macd.bearishCross()) {

            result.addScore(15);

            result.setDirection(ReversalDirection.BEARISH);

            result.addReason(
                    ReversalReason.MACD_BEARISH_DIVERGENCE);

            return;
        }

        if (macd.bullishCross()) {

            result.addScore(15);

            result.setDirection(ReversalDirection.BULLISH);

            result.addReason(
                    ReversalReason.MACD_BULLISH_DIVERGENCE);
        }
    }

}