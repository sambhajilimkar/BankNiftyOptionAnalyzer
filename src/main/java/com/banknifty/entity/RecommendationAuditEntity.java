package com.banknifty.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable recommendation snapshot; outcome columns are filled by the backtest evaluator. */
@Entity
@Table(name = "recommendation_audit")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationAuditEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false) private LocalDateTime generatedAt;
	@Column(nullable = false, length = 32) private String action;
	@Column(nullable = false, length = 64) private String instrument;
	@Column(length = 128) private String winnerSymbol;
	private Double winnerScore;
	@Column(nullable = false, length = 24) private String evaluationStatus;
	private LocalDateTime evaluateAt5m;
	private LocalDateTime evaluateAt15m;
	private LocalDateTime evaluateAt30m;
	private LocalDateTime evaluateAt60m;
	@Lob @Column(nullable = false) private String payloadJson;
}
