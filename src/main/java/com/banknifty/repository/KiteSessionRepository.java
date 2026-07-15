package com.banknifty.repository;

import com.banknifty.entity.KiteSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KiteSessionRepository extends JpaRepository<KiteSessionEntity, Long> {

	Optional<KiteSessionEntity> findByActiveTrue();

}