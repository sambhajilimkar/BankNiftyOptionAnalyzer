package com.banknifty.recommendation.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.recommendation.model.OptionAnalysis;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationValidationService {

	private final RecommendationValidationEngine validationEngine;

	/**
	 * Validate Top-5 recommendations.
	 */
	public List<OptionAnalysis> validateRecommendations(AnalysisContext context, List<OptionAnalysis> recommendations) {

		if (recommendations == null || recommendations.isEmpty()) {
			return List.of();
		}

		return recommendations.stream().filter(r -> isValid(context, r)).collect(Collectors.toList());
	}

	/**
	 * Validate single recommendation.
	 */
	public boolean isValid(AnalysisContext context, OptionAnalysis analysis) {

		RecommendationValidationResult result = validationEngine.validate(context, analysis);

		if (!result.isValid()) {

			result.getRejectedReasons().forEach(reason -> analysis.addReason("Rejected: " + reason));

			return false;
		}

		return true;
	}

}