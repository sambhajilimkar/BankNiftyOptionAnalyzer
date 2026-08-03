package com.banknifty.backtest.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banknifty.backtest.entity.RecommendationHistory;

@Repository
public interface RecommendationHistoryRepository
        extends JpaRepository<RecommendationHistory, Long> {

    List<RecommendationHistory> findByRecommendationTimeBetween(
            LocalDateTime from,
            LocalDateTime to);

    List<RecommendationHistory> findByStatus(String status);

}