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

    private static final BigDecimal FAILURE_DISTANCE = BigDecimal.valueOf(0.30);

    @Override
    public void detect(ReversalContext context,
                       ReversalResult result) {

        IndicatorSnapshot snapshot = context.getIndicators();

        if (snapshot == null) {
            return;
        }

        VWAPResult vwap = snapshot.vwap();

        if (vwap == null) {
            return;
        }

        /*
         * Price attempted a breakout but
         * remained very close to VWAP.
         */
        if (vwap.breakout()
                && vwap.distance() != null
                && vwap.distance().abs().compareTo(FAILURE_DISTANCE) <= 0) {

            result.addScore(12);

            if (vwap.aboveVWAP()) {
                result.setDirection(ReversalDirection.BEARISH);
            } else {
                result.setDirection(ReversalDirection.BULLISH);
            }

            result.addReason(ReversalReason.VWAP_BREAKDOWN);

            return;
        }

        /*
         * Healthy pullback to VWAP.
         */
        if (vwap.pullback()) {

            result.addScore(5);

            if (vwap.aboveVWAP()) {

                result.setDirection(ReversalDirection.BULLISH);

                result.addReason(ReversalReason.VWAP_BREAKOUT);

            } else {

                result.setDirection(ReversalDirection.BEARISH);

                result.addReason(ReversalReason.VWAP_BREAKDOWN);
            }
        }
    }
}