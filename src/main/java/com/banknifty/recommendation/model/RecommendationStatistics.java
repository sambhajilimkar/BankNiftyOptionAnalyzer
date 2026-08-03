package com.banknifty.recommendation.model;

import lombok.Builder;

@Builder
public record RecommendationStatistics(

        int contractsAnalysed,

        int qualifiedContracts,

        int rejectedContracts,

        double averageScore,

        double winnerScore

) {
}