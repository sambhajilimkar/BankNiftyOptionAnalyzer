package com.banknifty.recommendation.pipeline;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.enums.OptionType;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.recommendation.engine.OptionAnalysisEngine;
import com.banknifty.recommendation.engine.RankingEngine;
import com.banknifty.recommendation.context.RecommendationContext;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;
import com.banknifty.recommendation.model.RankedContract;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.RejectedContract;
import com.banknifty.recommendation.model.ScoreBreakdown;
import com.banknifty.recommendation.service.OptionChainAnalyzer;
import com.banknifty.recommendation.validation.RecommendationValidationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationPipelineService {

	private final OptionUniverseService optionUniverseService;

	private final OptionChainAnalyzer optionChainAnalyzer;

	private final OptionAnalysisEngine optionAnalysisEngine;

	private final RankingEngine rankingEngine;

	private final RecommendationValidationService validationService;

	public RecommendationPipeline analyse(RecommendationContext context) {

		RecommendationRequest request = context.getRequest();

		BigDecimal spot = context.getSpot();

		AnalysisContext analysisContext = context.getAnalysisContext();

		OptionType expectedDirection = context.getSignal().getOptionType();

		/*
		 * --------------------------------------------------------- Analyse complete
		 * option universe ---------------------------------------------------------
		 */

		List<OptionQuote> quotes = optionUniverseService.loadUniverse(request);

		List<OptionAnalysis> all = optionChainAnalyzer.analyze(quotes, spot).stream()
				.map(candidate -> optionAnalysisEngine.analyze(analysisContext, candidate)).toList();

		/*
		 * --------------------------------------------------------- Apply trade
		 * preference filter ---------------------------------------------------------
		 */

		List<OptionAnalysis> eligible = all.stream().filter(a -> matchesTradePreferences(a.getCandidate(), request))
				.toList();

		/*
		 * --------------------------------------------------------- Validation
		 * ---------------------------------------------------------
		 */

		List<OptionAnalysis> validatedEligible = validationService.validateRecommendations(analysisContext, eligible);

		/*
		 * --------------------------------------------------------- Final Ranking
		 * ---------------------------------------------------------
		 */

		List<OptionAnalysis> ranked = rankingEngine.top(validatedEligible, analysisContext, 5);

		/*
		 * --------------------------------------------------------- Top Contracts
		 * ---------------------------------------------------------
		 */

		List<RankedContract> top = ranked.stream().map(this::rankedContract).toList();

		/*
		 * --------------------------------------------------------- Rejected Contracts
		 * ---------------------------------------------------------
		 */

		List<RejectedContract> rejected = all.stream().filter(a -> !validatedEligible.contains(a)).limit(20)
				.map(a -> rejectedContract(a, expectedDirection, request)).toList();

		/*
		 * --------------------------------------------------------- Best contract
		 * matching detected direction
		 * ---------------------------------------------------------
		 */

		OptionAnalysis directionalBest = ranked.stream()
				.filter(a -> a.getCandidate().getOptionType() == expectedDirection).findFirst().orElse(null);

		return RecommendationPipeline.builder().all(all).eligible(eligible).validatedEligible(validatedEligible)
				.ranked(ranked).directionalBest(directionalBest).top(top).rejected(rejected).build();
	}

	/*
	 * --------------------------------------------------------- Helper methods
	 * copied from DefaultRecommendationEngine
	 * ---------------------------------------------------------
	 */

	private boolean matchesTradePreferences(OptionCandidate candidate, RecommendationRequest request) {

		int distance = candidate.getStrikeDistance() == null ? Integer.MAX_VALUE : candidate.getStrikeDistance();

		return switch (request.tradingStyle()) {

		case SCALPING -> candidate.isAtm();

		case INTRADAY ->
			candidate.isAtm() || (request.riskProfile() == com.banknifty.enums.RiskProfile.AGGRESSIVE && distance <= 1);

		case SWING -> candidate.isItm() && distance <= preferredItmDistance(request.riskProfile(), 2);

		case POSITIONAL -> candidate.isItm() && distance <= preferredItmDistance(request.riskProfile(), 3);
		};
	}

	private int preferredItmDistance(com.banknifty.enums.RiskProfile profile, int baseDistance) {

		return switch (profile) {

		case CONSERVATIVE -> 1;

		case MODERATE, BALANCED -> baseDistance;

		case AGGRESSIVE -> baseDistance + 1;
		};
	}

	private RankedContract rankedContract(OptionAnalysis analysis) {

		var candidate = analysis.getCandidate();

		return new RankedContract(analysis.getRank(), candidate.getTradingSymbol(), candidate.getExpiry(),
				candidate.getOptionType(), candidate.getStrike(), candidate.getPremium(), analysis.getTotalScore(),
				analysis.getConfidence(), analysis.getEntry(), analysis.getStopLoss(), analysis.getTarget1(),
				analysis.getTarget2(), grade(analysis.getTotalScore()), scoreBreakdown(analysis),
				List.copyOf(analysis.getReasons()));
	}

	private ScoreBreakdown scoreBreakdown(OptionAnalysis analysis) {

		double technical = percent(
				analysis.getTrendScore() + analysis.getSupportResistanceScore() + analysis.getPivotScore(), 35);

		double setup = percent(analysis.getStrikeScore() + analysis.getExpiryScore() + analysis.getVolatilityScore(),
				25);

		return new ScoreBreakdown(technical, setup, percent(analysis.getOpenInterestScore(), 10),
				percent(analysis.getGreekScore(), 5), percent(analysis.getLiquidityScore(), 12),
				percent(analysis.getRiskRewardScore(), 5), analysis.getTotalScore(), analysis.getTrendScore(),
				analysis.getSupportResistanceScore(), analysis.getPivotScore(), analysis.getStrikeScore(),
				analysis.getExpiryScore(), analysis.getVolatilityScore());
	}

	private double percent(double value, double maximum) {

		return Math.round(Math.min(100, value * 100 / maximum) * 10.0) / 10.0;
	}

	private String grade(double score) {

		if (score >= 95)
			return "A+";

		if (score >= 90)
			return "A";

		if (score >= 85)
			return "B+";

		if (score >= 80)
			return "B";

		return "C";
	}

	private RejectedContract rejectedContract(OptionAnalysis analysis, OptionType expectedType,
			RecommendationRequest request) {

		var candidate = analysis.getCandidate();

		String reason = candidate.getOptionType() != expectedType
				? "Direction conflicts with the confirmed " + expectedType + " market setup"
				: "Does not match " + request.tradingStyle().name().toLowerCase() + " strike preference";

		return new RejectedContract(candidate.getTradingSymbol(), candidate.getOptionType(), candidate.getStrike(),
				reason);
	}
}