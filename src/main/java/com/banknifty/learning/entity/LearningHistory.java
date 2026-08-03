package com.banknifty.learning.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Recommendation History ID
     */
    private Long recommendationId;

    /**
     * Trade Outcome ID
     */
    private Long tradeOutcomeId;

    /**
     * Strategy Name
     */
    private String strategy;

    /**
     * WIN / LOSS
     */
    private String result;

    /**
     * Accuracy before learning
     */
    private double previousAccuracy;

    /**
     * Accuracy after learning
     */
    private double currentAccuracy;

    /**
     * Score before learning
     */
    private double previousScore;

    /**
     * Score after learning
     */
    private double currentScore;

    /**
     * Weight Snapshot
     */
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

    /**
     * Remarks
     */
    @Column(length = 1000)
    private String remarks;

    /**
     * Learning Timestamp
     */
    private LocalDateTime learnedAt;

}