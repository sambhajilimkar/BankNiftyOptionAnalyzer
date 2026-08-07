package com.banknifty.recommendation.model;

import java.time.LocalDateTime;
import java.util.List;

import com.banknifty.analysis.prediction.PredictionSummary;

/**
 * V2 API response.
 */
public record RecommendationResponse(

		LocalDateTime generatedAt,

		TradeRecommendation winner,

		List<RankedContract> topContracts,

		List<RejectedContract> rejectedContracts,

		MarketSummary marketSummary,

		TradeSetup tradeSetup,

		WinnerExplanation winnerExplanation,

		PortfolioAllocation portfolioAllocation,

		RiskPlan riskPlan,

		/*
		 * ----------------------------- Prediction -----------------------------
		 */

		PredictionSummary prediction,

		/*
		 * ----------------------------- Reversal Analysis -----------------------------
		 */

		double continuationProbability,

		double reversalProbability,

		boolean reversalDetected,

		String reversalStrength,

		/*
		 * ----------------------------- Tracking -----------------------------
		 */

		String trackingStatus

) {
}