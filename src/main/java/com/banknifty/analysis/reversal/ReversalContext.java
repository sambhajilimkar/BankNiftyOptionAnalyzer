package com.banknifty.analysis.reversal;

import java.util.Collections;
import java.util.List;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReversalContext {

	/**
	 * Historical candles used for reversal detection.
	 */
	@Builder.Default
	private final List<Candle> candles = Collections.emptyList();

	/**
	 * Latest indicator snapshot.
	 */
	private final IndicatorSnapshot indicators;

	/**
	 * Analysis context from recommendation engine.
	 */
	private final AnalysisContext analysisContext;

	/**
	 * Number of available candles.
	 */
	public int candleCount() {
		return candles == null ? 0 : candles.size();
	}

	/**
	 * Whether enough candles exist for reversal analysis.
	 */
	public boolean hasMinimumCandles(int minimum) {
		return candleCount() >= minimum;
	}

	/**
	 * Latest candle.
	 */
	public Candle latestCandle() {

		if (candles == null || candles.isEmpty()) {
			return null;
		}

		return candles.get(candles.size() - 1);
	}

	/**
	 * Candle at lookback position.
	 */
	public Candle candle(int lookback) {

		if (candles == null || candles.isEmpty()) {
			return null;
		}

		int index = candles.size() - 1 - lookback;

		if (index < 0 || index >= candles.size()) {
			return null;
		}

		return candles.get(index);
	}

	/**
	 * Whether indicator snapshot is available.
	 */
	public boolean hasIndicators() {
		return indicators != null;
	}

	/**
	 * Whether analysis context is available.
	 */
	public boolean hasAnalysisContext() {
		return analysisContext != null;
	}

	/**
	 * Overall validation.
	 */
	public boolean isValid() {
		return hasIndicators() && hasAnalysisContext() && candleCount() > 0;
	}
}