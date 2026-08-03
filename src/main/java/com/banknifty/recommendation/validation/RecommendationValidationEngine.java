package com.banknifty.recommendation.validation;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;

@Service
public class RecommendationValidationEngine {

    private static final double MIN_SCORE = 70;
    private static final double MIN_CONFIDENCE = 70;
    private static final double MIN_PROBABILITY = 75;
    private static final double MIN_LIQUIDITY = 70;
    private static final double MIN_RISK_REWARD = 5;

    public RecommendationValidationResult validate(
            AnalysisContext context,
            OptionAnalysis analysis) {

        RecommendationValidationResult result =
                RecommendationValidationResult.builder().build();

        if (analysis == null || analysis.getCandidate() == null) {

            result.reject("Analysis unavailable");
            return result;
        }

        OptionCandidate candidate = analysis.getCandidate();

        /*
         * Total Score
         */
        if (analysis.getTotalScore() < MIN_SCORE) {
            result.reject("Low Total Score");
        }

        /*
         * Confidence
         */
        if (analysis.getConfidence() < MIN_CONFIDENCE) {
            result.reject("Low Confidence");
        }

        /*
         * Probability
         */
        if (analysis.getProbabilityScore() < MIN_PROBABILITY) {
            result.reject("Low Probability");
        }

        /*
         * Liquidity
         */
        if (analysis.getLiquidityScore() < MIN_LIQUIDITY) {
            result.reject("Poor Liquidity");
        }

        /*
         * Risk Reward
         */
        if (analysis.getRiskRewardScore() < MIN_RISK_REWARD) {
            result.reject("Poor Risk Reward");
        }

        /*
         * Premium
         */
        if (candidate.getPremium() == null
                || candidate.getPremium().doubleValue() <= 0) {

            result.reject("Invalid Premium");
        }

        /*
         * Spread
         */
        if (candidate.getSpreadPercentage() != null
                && candidate.getSpreadPercentage().doubleValue() > 1.5) {

            result.reject("High Bid/Ask Spread");
        }

        /*
         * Open Interest
         */
        if (candidate.getOpenInterest() < 10000) {

            result.reject("Low Open Interest");
        }

        /*
         * Trend vs Institutional Bias
         */
        if (context != null
                && context.getInstitutionalAnalysis() != null
                && context.getInstitutionalAnalysis().getMarketBias() != null
                && context.getMarketBias() != null) {

            if (!context.getInstitutionalAnalysis()
                    .getMarketBias()
                    .name()
                    .contains(context.getMarketBias().name())) {

                result.reject("Institutional Bias Mismatch");
            }
        }

        /*
         * Final Decision
         */
        if (!result.hasErrors()) {
            result.approve();
        }

        return result;
    }
}