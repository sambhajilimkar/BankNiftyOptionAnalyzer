package com.banknifty.recommendation.probability;

import org.springframework.stereotype.Service;

import com.banknifty.recommendation.model.OptionAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProbabilityEngine {

    private final ProbabilityCalculator calculator;

    /**
     * Calculates probability and updates OptionAnalysis.
     */
    public void evaluate(OptionAnalysis analysis) {

        if (analysis == null) {
            return;
        }

        ProbabilityResult result = calculator.calculate(analysis);

        analysis.setProbabilityScore(result.probability());

        analysis.addReason(
                String.format(
                        "Probability %.2f%% (%s)",
                        result.probability(),
                        result.grade()));

        /*
         * Small bonus based on probability.
         */
        if (result.probability() >= 90) {

            analysis.addScore(3);
            analysis.addReason("Very High Probability");

        } else if (result.probability() >= 80) {

            analysis.addScore(2);
            analysis.addReason("High Probability");

        } else if (result.probability() >= 70) {

            analysis.addScore(1);
            analysis.addReason("Good Probability");
        }

        analysis.calculateConfidence();

        log.debug(
                "Probability calculated for {} : {}%",
                analysis.getCandidate() == null
                        ? "UNKNOWN"
                        : analysis.getCandidate().getTradingSymbol(),
                result.probability());
    }
}