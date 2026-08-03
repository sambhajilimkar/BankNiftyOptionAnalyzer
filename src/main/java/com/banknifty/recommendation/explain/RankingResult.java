package com.banknifty.recommendation.explain;

import com.banknifty.recommendation.explain.RecommendationGrade;
import com.banknifty.recommendation.explain.RecommendationReason;
import com.banknifty.recommendation.explain.ScoreBreakdown;
import lombok.Builder;

import java.util.List;

/**
 * Immutable result produced by RankingEngine.
 *
 * This class represents the complete ranking outcome for a
 * single option contract.
 */
@Builder
public record RankingResult(

        /**
         * Final normalized score (0-100).
         */
        int finalScore,

        /**
         * Recommendation confidence.
         */
        int confidence,

        /**
         * Recommendation grade.
         */
        RecommendationGrade grade,

        /**
         * Detailed score breakdown.
         */
        ScoreBreakdown scoreBreakdown,

        /**
         * Positive ranking reasons.
         */
        List<RecommendationReason> strengths,

        /**
         * Negative ranking reasons.
         */
        List<RecommendationReason> weaknesses

) {

    /**
     * Returns true when this contract is suitable for execution.
     */
    public boolean tradable() {
        return grade != null && grade.tradable();
    }

    /**
     * Returns the strongest reason.
     */
    public RecommendationReason strongestReason() {

        if (strengths == null || strengths.isEmpty()) {
            return null;
        }

        return strengths.stream()
                .max((a, b) -> Integer.compare(a.importance(), b.importance()))
                .orElse(null);
    }

    /**
     * Returns the biggest weakness.
     */
    public RecommendationReason biggestWeakness() {

        if (weaknesses == null || weaknesses.isEmpty()) {
            return null;
        }

        return weaknesses.stream()
                .max((a, b) -> Integer.compare(a.importance(), b.importance()))
                .orElse(null);
    }
}