package com.banknifty.engine;

import com.banknifty.enums.SignalType;
import com.banknifty.model.OptionRecommendation;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Selects the best option recommendation based on confidence.
 */
@Component
public class OptionChainEngine {

	/**
	 * Returns the highest-confidence recommendation.
	 */
	public OptionRecommendation selectBest(List<OptionRecommendation> recommendations) {

		if (recommendations == null || recommendations.isEmpty()) {
			throw new IllegalArgumentException("Option recommendations cannot be empty.");
		}

		return recommendations.stream().max(Comparator.comparingInt(OptionRecommendation::confidence)).orElseThrow();
	}

	/**
	 * Filters BUY CE recommendations.
	 */
	public List<OptionRecommendation> buyCE(List<OptionRecommendation> recommendations) {
		return recommendations.stream().filter(r -> r.signal() == SignalType.BUY_CE).toList();
	}

	/**
	 * Filters BUY PE recommendations.
	 */
	public List<OptionRecommendation> buyPE(List<OptionRecommendation> recommendations) {
		return recommendations.stream().filter(r -> r.signal() == SignalType.BUY_PE).toList();
	}
}
