package com.banknifty.recommendation.probability;

import lombok.Builder;

/**
 * Probability calculation result.
 */
@Builder
public record ProbabilityResult(

        double probability,

        String grade,

        String recommendation,

        boolean tradable

) {

    public static ProbabilityResult of(double probability) {

        double value = Math.max(0, Math.min(probability, 100));

        String grade;
        String recommendation;
        boolean tradable;

        if (value >= 90) {
            grade = "A+";
            recommendation = "STRONG BUY";
            tradable = true;

        } else if (value >= 80) {
            grade = "A";
            recommendation = "BUY";
            tradable = true;

        } else if (value >= 70) {
            grade = "B";
            recommendation = "BUY";
            tradable = true;

        } else if (value >= 60) {
            grade = "C";
            recommendation = "WATCH";
            tradable = false;

        } else {
            grade = "D";
            recommendation = "AVOID";
            tradable = false;
        }

        return ProbabilityResult.builder()
                .probability(value)
                .grade(grade)
                .recommendation(recommendation)
                .tradable(tradable)
                .build();
    }
}