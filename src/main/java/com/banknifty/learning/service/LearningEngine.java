package com.banknifty.learning.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banknifty.backtest.entity.TradeOutcome;
import com.banknifty.backtest.entity.TradeStatus;
import com.banknifty.backtest.repository.TradeOutcomeRepository;
import com.banknifty.learning.entity.LearningHistory;
import com.banknifty.learning.model.LearningWeights;
import com.banknifty.learning.repository.LearningHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LearningEngine {

    private final TradeOutcomeRepository tradeOutcomeRepository;

    private final LearningHistoryRepository learningHistoryRepository;

    private LearningWeights currentWeights =
            LearningWeights.defaultWeights();

    /**
     * Execute one learning cycle.
     */
    public void learn() {

        List<TradeOutcome> completedTrades = tradeOutcomeRepository.findAll()
                .stream()
                .filter(this::completed)
                .toList();

        if (completedTrades.isEmpty()) {
            log.info("No completed trades available for learning.");
            return;
        }

        long wins = completedTrades.stream()
                .filter(this::winner)
                .count();

        double accuracy =
                (wins * 100.0) / completedTrades.size();

        LearningWeights previous = copy(currentWeights);

        adjustWeights(accuracy);

        LearningHistory history = LearningHistory.builder()
                .strategy("DEFAULT")
                .result(accuracy >= 70 ? "WIN" : "LOSS")
                .previousAccuracy(previousAccuracy())
                .currentAccuracy(accuracy)
                .previousScore(total(previous))
                .currentScore(total(currentWeights))
                .trendWeight(currentWeights.getTrendWeight())
                .institutionalWeight(currentWeights.getInstitutionalWeight())
                .openInterestWeight(currentWeights.getOpenInterestWeight())
                .liquidityWeight(currentWeights.getLiquidityWeight())
                .greekWeight(currentWeights.getGreekWeight())
                .volatilityWeight(currentWeights.getVolatilityWeight())
                .supportResistanceWeight(currentWeights.getSupportResistanceWeight())
                .pivotWeight(currentWeights.getPivotWeight())
                .expiryWeight(currentWeights.getExpiryWeight())
                .riskRewardWeight(currentWeights.getRiskRewardWeight())
                .remarks("Automatic learning cycle")
                .learnedAt(LocalDateTime.now())
                .build();

        learningHistoryRepository.save(history);

        log.info("Learning completed. Accuracy={}%", accuracy);
    }

    public LearningWeights currentWeights() {
        return currentWeights;
    }

    private void adjustWeights(double accuracy) {

        if (accuracy >= 80) {

            currentWeights.setTrendWeight(
                    currentWeights.getTrendWeight() + 0.5);

            currentWeights.setInstitutionalWeight(
                    currentWeights.getInstitutionalWeight() + 0.5);

        } else if (accuracy < 60) {

            currentWeights.setTrendWeight(
                    Math.max(10,
                            currentWeights.getTrendWeight() - 0.5));

            currentWeights.setInstitutionalWeight(
                    Math.max(10,
                            currentWeights.getInstitutionalWeight() - 0.5));

            currentWeights.setRiskRewardWeight(
                    currentWeights.getRiskRewardWeight() + 0.5);
        }
    }

    private boolean completed(TradeOutcome trade) {

        return trade.getStatus() != TradeStatus.OPEN;
    }

    private boolean winner(TradeOutcome trade) {

        return trade.getStatus() == TradeStatus.TARGET1_HIT
                || trade.getStatus() == TradeStatus.TARGET2_HIT
                || trade.getStatus() == TradeStatus.EXITED;
    }

    private double previousAccuracy() {

        return learningHistoryRepository.findAll()
                .stream()
                .reduce((a, b) -> b)
                .map(LearningHistory::getCurrentAccuracy)
                .orElse(0.0);
    }

    private LearningWeights copy(LearningWeights w) {

        return LearningWeights.builder()
                .trendWeight(w.getTrendWeight())
                .institutionalWeight(w.getInstitutionalWeight())
                .openInterestWeight(w.getOpenInterestWeight())
                .liquidityWeight(w.getLiquidityWeight())
                .greekWeight(w.getGreekWeight())
                .volatilityWeight(w.getVolatilityWeight())
                .supportResistanceWeight(w.getSupportResistanceWeight())
                .pivotWeight(w.getPivotWeight())
                .expiryWeight(w.getExpiryWeight())
                .riskRewardWeight(w.getRiskRewardWeight())
                .build();
    }

    private double total(LearningWeights w) {

        return w.getTrendWeight()
                + w.getInstitutionalWeight()
                + w.getOpenInterestWeight()
                + w.getLiquidityWeight()
                + w.getGreekWeight()
                + w.getVolatilityWeight()
                + w.getSupportResistanceWeight()
                + w.getPivotWeight()
                + w.getExpiryWeight()
                + w.getRiskRewardWeight();
    }

}