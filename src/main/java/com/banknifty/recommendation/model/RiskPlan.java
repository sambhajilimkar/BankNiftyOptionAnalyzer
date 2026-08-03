package com.banknifty.recommendation.model;

import java.math.BigDecimal;

/** Position sizing and loss limits calculated from the selected option premium. */
public record RiskPlan(BigDecimal entry, BigDecimal stopLoss, BigDecimal target1,
		BigDecimal target2, BigDecimal riskPerLot, BigDecimal capitalAllocated,
		BigDecimal maximumLoss, BigDecimal rewardToRisk, int lots, int quantity) {
}
