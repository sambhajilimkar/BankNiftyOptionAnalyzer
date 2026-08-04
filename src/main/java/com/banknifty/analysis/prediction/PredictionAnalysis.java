package com.banknifty.analysis.prediction;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PredictionAnalysis {

	private final PredictionDirection direction;

	private final PredictionStrength strength;

	private final double confidence;

	private final double expectedMove;

	private final double continuationProbability;

	private final double reversalProbability;

	@Builder.Default
	private final List<String> reasons = new ArrayList<>();

	public boolean bullish() {
		return direction == PredictionDirection.BULLISH;
	}

	public boolean bearish() {
		return direction == PredictionDirection.BEARISH;
	}

	public boolean sideways() {
		return direction == PredictionDirection.SIDEWAYS;
	}

	public boolean reversal() {
		return direction == PredictionDirection.REVERSAL;
	}
}