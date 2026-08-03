package com.banknifty.backtest.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banknifty.backtest.entity.RecommendationHistory;
import com.banknifty.backtest.entity.TradeOutcome;
import com.banknifty.backtest.entity.TradeStatus;
import com.banknifty.backtest.repository.RecommendationHistoryRepository;
import com.banknifty.backtest.repository.TradeOutcomeRepository;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BacktestService {

    private final RecommendationHistoryRepository historyRepository;

    private final TradeOutcomeRepository tradeOutcomeRepository;

    /**
     * Save every recommendation.
     */
    public RecommendationHistory saveRecommendation(OptionAnalysis analysis) {

        if (analysis == null || analysis.getCandidate() == null) {
            return null;
        }

        OptionCandidate c = analysis.getCandidate();

        RecommendationHistory history = RecommendationHistory.builder()
                .recommendationTime(LocalDateTime.now())
                .tradingSymbol(c.getTradingSymbol())
                .optionType(c.getOptionType())
                .strike(c.getStrike())
                .spotPrice(c.getSpotPrice())
                .premium(c.getPremium())
                .totalScore(analysis.getTotalScore())
                .confidence(analysis.getConfidence())
                .entry(analysis.getEntry())
                .stopLoss(analysis.getStopLoss())
                .target1(analysis.getTarget1())
                .target2(analysis.getTarget2())
                .reasons(String.join(" | ", analysis.getReasons()))
                .status("OPEN")
                .build();

        history = historyRepository.save(history);

        TradeOutcome outcome = TradeOutcome.builder()
                .recommendation(history)
                .entryTime(LocalDateTime.now())
                .entryPrice(analysis.getEntry())
                .status(TradeStatus.OPEN)
                .build();

        tradeOutcomeRepository.save(outcome);

        return history;
    }

    /**
     * Update completed trade.
     */
    public void closeTrade(
            Long recommendationId,
            BigDecimal exitPrice,
            TradeStatus status) {

        TradeOutcome outcome = tradeOutcomeRepository.findById(recommendationId)
                .orElse(null);

        if (outcome == null) {
            return;
        }

        outcome.setExitTime(LocalDateTime.now());
        outcome.setExitPrice(exitPrice);
        outcome.setStatus(status);

        if (outcome.getEntryPrice() != null
                && exitPrice != null) {

            BigDecimal pnl =
                    exitPrice.subtract(outcome.getEntryPrice());

            outcome.setPnl(pnl);

            if (outcome.getEntryPrice().doubleValue() != 0) {

                double pnlPct =
                        pnl.doubleValue()
                                * 100
                                / outcome.getEntryPrice().doubleValue();

                outcome.setPnlPercentage(pnlPct);
            }
        }

        tradeOutcomeRepository.save(outcome);
    }

    public long totalRecommendations() {
        return historyRepository.count();
    }

    public long totalTrades() {
        return tradeOutcomeRepository.count();
    }

}