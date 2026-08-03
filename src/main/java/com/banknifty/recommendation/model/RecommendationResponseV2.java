package com.banknifty.recommendation.model;

import java.time.LocalDateTime;
import java.util.List;

/** V2 API response: decision, alternatives, exclusions, risk and tracking metadata. */
public record RecommendationResponseV2(LocalDateTime generatedAt, TradeRecommendation winner,
		List<RankedContract> topContracts, List<RejectedContract> rejectedContracts,
		MarketSummary marketSummary, TradeSetup tradeSetup, WinnerExplanation winnerExplanation,
		PortfolioAllocation portfolioAllocation, RiskPlan riskPlan, String trackingStatus) {
}
