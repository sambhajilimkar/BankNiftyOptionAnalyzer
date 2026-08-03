package com.banknifty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.banknifty.entity.RecommendationAuditEntity;

public interface RecommendationAuditRepository extends JpaRepository<RecommendationAuditEntity, Long> {
}
