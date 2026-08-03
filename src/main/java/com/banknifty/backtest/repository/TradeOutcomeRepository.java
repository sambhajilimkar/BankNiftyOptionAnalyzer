package com.banknifty.backtest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banknifty.backtest.entity.TradeOutcome;
import com.banknifty.backtest.entity.TradeStatus;

@Repository
public interface TradeOutcomeRepository
        extends JpaRepository<TradeOutcome, Long> {

    List<TradeOutcome> findByStatus(TradeStatus status);

    List<TradeOutcome> findByTarget1HitTrue();

    List<TradeOutcome> findByTarget2HitTrue();

    List<TradeOutcome> findByStopLossHitTrue();

}