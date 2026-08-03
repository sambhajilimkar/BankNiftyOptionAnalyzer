package com.banknifty.learning.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningWeights {

    private double trendWeight;

    private double institutionalWeight;

    private double openInterestWeight;

    private double liquidityWeight;

    private double greekWeight;

    private double volatilityWeight;

    private double supportResistanceWeight;

    private double pivotWeight;

    private double expiryWeight;

    private double riskRewardWeight;

    public static LearningWeights defaultWeights() {

        return LearningWeights.builder()
                .trendWeight(25)
                .institutionalWeight(20)
                .openInterestWeight(15)
                .liquidityWeight(15)
                .greekWeight(5)
                .volatilityWeight(5)
                .supportResistanceWeight(5)
                .pivotWeight(5)
                .expiryWeight(3)
                .riskRewardWeight(2)
                .build();
    }

}