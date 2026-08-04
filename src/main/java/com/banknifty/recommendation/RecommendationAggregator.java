package com.banknifty.recommendation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.prediction.PredictionAnalysis;
import com.banknifty.analysis.prediction.PredictionSummary;
import com.banknifty.analysis.reversal.ReversalAnalysis;
import com.banknifty.analysis.reversal.ReversalStrength;
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

	/**
	 * Backward compatible overload.
	 */
	public RecommendationResponseV2 aggregate(TradeRecommendation winner, List<RankedContract> topContracts,
			List<RejectedContract> rejectedContracts, MarketSummary marketSummary, TradeSetup tradeSetup,
			WinnerExplanation winnerExplanation, PortfolioAllocation portfolioAllocation, RiskPlan riskPlan,
			String trackingStatus) {

		return aggregate(winner, topContracts, rejectedContracts, marketSummary, tradeSetup, winnerExplanation,
				portfolioAllocation, riskPlan, null, null, trackingStatus);
	}

	/**
	 * Overload with reversal analysis.
	 */
	public RecommendationResponseV2 aggregate(TradeRecommendation winner, List<RankedContract> topContracts,
			List<RejectedContract> rejectedContracts, MarketSummary marketSummary, TradeSetup tradeSetup,
			WinnerExplanation winnerExplanation, PortfolioAllocation portfolioAllocation, RiskPlan riskPlan,
			ReversalAnalysis reversal, String trackingStatus) {

		return aggregate(winner, topContracts, rejectedContracts, marketSummary, tradeSetup, winnerExplanation,
				portfolioAllocation, riskPlan, null, reversal, trackingStatus);
	}

	/**
	 * Full V3 aggregation.
	 */
	public RecommendationResponseV2 aggregate(TradeRecommendation winner, List<RankedContract> topContracts,
			List<RejectedContract> rejectedContracts, MarketSummary marketSummary, TradeSetup tradeSetup,
			WinnerExplanation winnerExplanation, PortfolioAllocation portfolioAllocation, RiskPlan riskPlan,
			PredictionAnalysis prediction, ReversalAnalysis reversal, String trackingStatus) {

		PredictionSummary predictionSummary = prediction == null ? null
				: PredictionSummary.builder().direction(prediction.getDirection()).strength(prediction.getStrength())
						.confidence(prediction.getConfidence()).expectedMove(prediction.getExpectedMove())
						.continuationProbability(prediction.getContinuationProbability())
						.reversalProbability(prediction.getReversalProbability()).build();

		RecommendationResponseV2 response = new RecommendationResponseV2(

				LocalDateTime.now(),

				winner,

				topContracts == null ? List.of() : List.copyOf(topContracts),

				rejectedContracts == null ? List.of() : List.copyOf(rejectedContracts),

				marketSummary,

				tradeSetup,

				winnerExplanation,

				portfolioAllocation,

				riskPlan,

				predictionSummary,

				reversal == null ? 0.0 : reversal.getContinuationProbability(),

				reversal == null ? 0.0 : reversal.getReversalProbability(),

				reversal != null && reversal.isReversalDetected(),

				reversal == null ? ReversalStrength.VERY_LOW.name() : reversal.getStrength().name(),

				trackingStatus);

		auditService.record(response);

		return response;
	}
}