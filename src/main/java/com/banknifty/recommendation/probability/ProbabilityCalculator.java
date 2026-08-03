package com.banknifty.recommendation.probability;

import org.springframework.stereotype.Component;

import com.banknifty.recommendation.model.OptionAnalysis;

/**
 * Calculates overall probability score from individual analysis scores.
 */
@Component
public class ProbabilityCalculator {

    public ProbabilityResult calculate(OptionAnalysis analysis) {

        if (analysis == null) {
            return ProbabilityResult.of(0);
        }

        double probability = 0;

        probability += weighted(analysis.getTrendScore(),
                ProbabilityWeights.TREND, 25);

        probability += weighted(analysis.getOpenInterestScore(),
                ProbabilityWeights.OPEN_INTEREST, 10);

        probability += weighted(analysis.getLiquidityScore(),
                ProbabilityWeights.LIQUIDITY, 12);

        probability += weighted(analysis.getGreekScore(),
                ProbabilityWeights.GREEKS, 5);

        probability += weighted(analysis.getRiskRewardScore(),
                ProbabilityWeights.RISK_REWARD, 5);

        probability += weighted(analysis.getVolatilityScore(),
                ProbabilityWeights.VOLATILITY, 5);

        /*
         * Institutional score is currently stored inside totalScore.
         * This will be separated in Milestone-2.
         */
        double institutionalScore =
                Math.min(analysis.getTotalScore() * 0.20, 20);

        probability += institutionalScore;

        probability = Math.max(0, Math.min(probability, 100));

        return ProbabilityResult.of(probability);
    }

    private double weighted(double actual,
                            double weight,
                            double maximumScore) {

        if (maximumScore <= 0) {
            return 0;
        }

        double normalized = Math.min(actual, maximumScore) / maximumScore;

        return normalized * weight;
    }
}