package com.banknifty.recommendation.explain;

import lombok.Builder;

/**
 * Detailed score breakdown for a ranked option contract.
 *
 * Instead of exposing only one final score, this object explains
 * how the final recommendation score was calculated.
 */
@Builder
public record ScoreBreakdown(

        /*
         * Core Technical Analysis
         */
        int technicalScore,

        /*
         * Trade Setup Quality
         */
        int setupScore,

        /*
         * Open Interest Analysis
         */
        int oiScore,

        /*
         * Greeks Analysis
         */
        int greeksScore,

        /*
         * Liquidity Analysis
         */
        int liquidityScore,

        /*
         * Bid / Ask Spread Quality
         */
        int spreadScore,

        /*
         * Risk Reward Quality
         */
        int riskRewardScore,

        /*
         * Institutional Activity
         */
        int institutionalScore,

        /*
         * Overall Recommendation Score
         */
        int finalScore,

        /*
         * Recommendation Grade
         */
        RecommendationGrade grade

) {

    /**
     * Returns true if the recommendation is suitable
     * for execution.
     */
    public boolean tradable() {
        return grade != null && grade.tradable();
    }

    /**
     * Percentage representation of the final score.
     */
    public int confidence() {
        return Math.max(0, Math.min(100, finalScore));
    }

    /**
     * Returns the strongest contributing component.
     */
    public String strongestArea() {

        int max = technicalScore;
        String area = "Technical";

        if (setupScore > max) {
            max = setupScore;
            area = "Setup";
        }

        if (oiScore > max) {
            max = oiScore;
            area = "Open Interest";
        }

        if (greeksScore > max) {
            max = greeksScore;
            area = "Greeks";
        }

        if (liquidityScore > max) {
            max = liquidityScore;
            area = "Liquidity";
        }

        if (spreadScore > max) {
            max = spreadScore;
            area = "Spread";
        }

        if (riskRewardScore > max) {
            max = riskRewardScore;
            area = "Risk Reward";
        }

        if (institutionalScore > max) {
            area = "Institutional";
        }

        return area;
    }

    /**
     * Returns the weakest contributing component.
     */
    public String weakestArea() {

        int min = technicalScore;
        String area = "Technical";

        if (setupScore < min) {
            min = setupScore;
            area = "Setup";
        }

        if (oiScore < min) {
            min = oiScore;
            area = "Open Interest";
        }

        if (greeksScore < min) {
            min = greeksScore;
            area = "Greeks";
        }

        if (liquidityScore < min) {
            min = liquidityScore;
            area = "Liquidity";
        }

        if (spreadScore < min) {
            min = spreadScore;
            area = "Spread";
        }

        if (riskRewardScore < min) {
            min = riskRewardScore;
            area = "Risk Reward";
        }

        if (institutionalScore < min) {
            area = "Institutional";
        }

        return area;
    }
}