package com.banknifty.learning.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banknifty.learning.entity.LearningHistory;

@Repository
public interface LearningHistoryRepository
        extends JpaRepository<LearningHistory, Long> {

    List<LearningHistory> findByStrategy(String strategy);

    List<LearningHistory> findByResult(String result);

    List<LearningHistory> findByLearnedAtBetween(
            LocalDateTime from,
            LocalDateTime to);

    long countByResult(String result);

}