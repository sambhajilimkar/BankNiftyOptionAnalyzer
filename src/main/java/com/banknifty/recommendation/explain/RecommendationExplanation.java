package com.banknifty.recommendation.explain;

import lombok.Builder;

import java.util.List;

/**
 * Base explanation model for recommendation responses.
 *
 * Winner, Top-5 candidates and rejected candidates all share
 * the same explanation structure.
 */
@Builder
public record RecommendationExplanation(

        /*
         * Rank assigned by RankingEngine.
         */
        int rank,

        /*
         * Trading symbol.
         */
        String tradingSymbol,

        /*
         * Final recommendation score.
         */
        int finalScore,

        /*
         * Recommendation confidence.
         */
        int confidence,

        /*
         * Grade.
         */
        RecommendationGrade grade,

        /*
         * Detailed score breakdown.
         */
        ScoreBreakdown scoreBreakdown,

        /*
         * Positive reasons.
         */
        List<RecommendationReason> strengths,

        /*
         * Negative reasons.
         */
        List<RecommendationReason> weaknesses,

        /*
         * One line explanation.
         */
        String summary

) {

    public boolean tradable() {
        return grade != null && grade.tradable();
    }

    public boolean winner() {
        return rank == 1;
    }

    public boolean rejected() {
        return !tradable();
    }
}