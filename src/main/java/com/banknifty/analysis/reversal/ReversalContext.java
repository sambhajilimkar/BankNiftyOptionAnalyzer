package com.banknifty.analysis.reversal;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReversalContext {

    private final List<Candle> candles;

    private final IndicatorSnapshot indicators;

    private final AnalysisContext analysisContext;

}