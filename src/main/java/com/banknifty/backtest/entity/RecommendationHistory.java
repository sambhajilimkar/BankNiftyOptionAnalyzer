package com.banknifty.backtest.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banknifty.enums.OptionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendation_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime recommendationTime;

	private String tradingSymbol;

	@Enumerated(EnumType.STRING)
	private OptionType optionType;

	private Integer strike;

	private BigDecimal spotPrice;

	private BigDecimal premium;

	private double totalScore;

	private double confidence;

	private BigDecimal entry;

	private BigDecimal stopLoss;

	private BigDecimal target1;

	private BigDecimal target2;

	@Column(length = 1000)
	private String reasons;

	private String status;

	private Long instrumentToken;
}