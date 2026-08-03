package com.banknifty.backtest.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestStatistics {

    private long totalTrades;

    private long winningTrades;

    private long losingTrades;

    private double winRate;

    private BigDecimal totalProfit;

    public static BacktestStatistics empty() {

        return BacktestStatistics.builder()
                .totalTrades(0)
                .winningTrades(0)
                .losingTrades(0)
                .winRate(0)
                .totalProfit(BigDecimal.ZERO)
                .build();
    }

}