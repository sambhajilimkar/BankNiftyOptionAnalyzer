package com.banknifty.backtest.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trade_outcome")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private RecommendationHistory recommendation;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private BigDecimal entryPrice;

    private BigDecimal exitPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private Boolean target1Hit;

    private Boolean target2Hit;

    private Boolean stopLossHit;

    private Integer holdingMinutes;

    private BigDecimal pnl;

    private Double pnlPercentage;

    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    @Column(length = 1000)
    private String remarks;
}