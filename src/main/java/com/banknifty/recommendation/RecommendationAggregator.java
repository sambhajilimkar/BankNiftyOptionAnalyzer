package com.banknifty.recommendation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.recommendation.model.MarketSummary;
import com.banknifty.recommendation.model.PortfolioAllocation;
import com.banknifty.recommendation.model.RankedContract;
import com.banknifty.recommendation.model.RecommendationResponseV2;
import com.banknifty.recommendation.model.RejectedContract;
import com.banknifty.recommendation.model.RiskPlan;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.model.TradeSetup;
import com.banknifty.recommendation.model.WinnerExplanation;

/**
 * Builds the externally visible V2 recommendation response.
 */
@Service
public class RecommendationAggregator {

	private final RecommendationAuditService auditService;

	public RecommendationAggregator(RecommendationAuditService auditService) {
		this.auditService = auditService;
	}

	public RecommendationResponseV2 aggregate(TradeRecommendation winner, List<RankedContract> topContracts,
			List<RejectedContract> rejectedContracts, MarketSummary marketSummary, TradeSetup tradeSetup,
			WinnerExplanation winnerExplanation, PortfolioAllocation portfolioAllocation, RiskPlan riskPlan,
			String trackingStatus) {

		RecommendationResponseV2 response = new RecommendationResponseV2(LocalDateTime.now(), winner,
				topContracts == null ? List.of() : List.copyOf(topContracts),
				rejectedContracts == null ? List.of() : List.copyOf(rejectedContracts), marketSummary, tradeSetup,
				winnerExplanation, portfolioAllocation, riskPlan, trackingStatus);

		auditService.record(response);

		return response;
	}
}