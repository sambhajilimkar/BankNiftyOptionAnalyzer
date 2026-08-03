package com.banknifty.analysis.reversal;

public interface ReversalDetector {

    void detect(ReversalContext context, ReversalResult result);

}