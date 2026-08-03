package com.banknifty.analysis.reversal;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReversalAnalysis {

    private final boolean reversalDetected;

    private final ReversalDirection currentTrend;

    private final ReversalDirection predictedTrend;

    private final double reversalProbability;

    private final double continuationProbability;

    private final ReversalStrength strength;

    private final List<ReversalReason> reasons;

}