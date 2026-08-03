package com.banknifty.recommendation.explain;

import lombok.Builder;

import java.util.List;

/**
 * Explainability model for every ranked option contract.
 *
 * Used for:
 * - Winner
 * - Top 5
 * - Watchlist
 * - Rejected
 */
@Builder
public record CandidateExplanation(

        int rank,

        String tradingSymbol,

        String optionType,

        int strike,

        int finalScore,

        int confidence,

        RecommendationGrade grade,

        ScoreBreakdown scoreBreakdown,

        List<RecommendationReason> strengths,

        List<RecommendationReason> weaknesses,

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

    public String displayGrade() {
        return grade != null ? grade.grade() : "";
    }
}