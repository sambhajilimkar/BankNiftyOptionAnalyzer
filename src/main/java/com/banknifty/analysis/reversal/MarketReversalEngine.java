package com.banknifty.analysis.reversal;

import com.banknifty.analysis.MarketBias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketReversalEngine {

    private final List<ReversalDetector> detectors;

    public ReversalAnalysis analyze(ReversalContext context) {

        ReversalResult result = new ReversalResult();

        for (ReversalDetector detector : detectors) {
            detector.detect(context, result);
        }

        double reversalProbability = Math.min(result.getScore(), 100.0);
        double continuationProbability = Math.max(0.0, 100.0 - reversalProbability);

        return ReversalAnalysis.builder()
                .reversalDetected(reversalProbability >= 60.0)
                .currentTrend(resolveCurrentTrend(context))
                .predictedTrend(result.getDirection())
                .reversalProbability(reversalProbability)
                .continuationProbability(continuationProbability)
                .strength(resolveStrength(reversalProbability))
                .reasons(result.getReasons())
                .build();
    }

    private ReversalDirection resolveCurrentTrend(ReversalContext context) {

        if (context == null
                || context.getAnalysisContext() == null
                || context.getAnalysisContext().getMarketBias() == null) {
            return ReversalDirection.NONE;
        }

        MarketBias bias = context.getAnalysisContext().getMarketBias();

        switch (bias) {

            case STRONG_BULLISH:
            case BULLISH:
                return ReversalDirection.BULLISH;

            case STRONG_BEARISH:
            case BEARISH:
                return ReversalDirection.BEARISH;

            case SIDEWAYS:
            default:
                return ReversalDirection.NONE;
        }
    }

    private ReversalStrength resolveStrength(double probability) {

        if (probability >= 85) {
            return ReversalStrength.VERY_HIGH;
        }

        if (probability >= 70) {
            return ReversalStrength.HIGH;
        }

        if (probability >= 50) {
            return ReversalStrength.MODERATE;
        }

        if (probability >= 30) {
            return ReversalStrength.LOW;
        }

        return ReversalStrength.VERY_LOW;
    }
}