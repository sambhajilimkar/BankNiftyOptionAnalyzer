package com.banknifty.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedOption {

	private Integer rank;

	private OptionCandidate candidate;

	private BigDecimal entry;

	private BigDecimal stopLoss;

	private BigDecimal target1;

	private BigDecimal target2;

	private List<String> reasons;
}