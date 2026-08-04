package com.banknifty.analysis.prediction;

import java.util.Collections;
import java.util.List;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PredictionContext {

	@Builder.Default
	private final List<Candle> candles = Collections.emptyList();

	private final IndicatorSnapshot indicators;

	private final AnalysisContext analysisContext;

}