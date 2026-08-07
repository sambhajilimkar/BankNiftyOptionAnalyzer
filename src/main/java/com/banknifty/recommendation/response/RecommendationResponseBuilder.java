package com.banknifty.recommendation.response;

import com.banknifty.backtest.service.BacktestService;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskProfile;
import com.banknifty.recommendation.RecommendationAggregator;
import com.banknifty.recommendation.TradeSetupBuilder;
import com.banknifty.recommendation.context.RecommendationContext;
import com.banknifty.recommendation.decision.RecommendationDecisionService;
import com.banknifty.recommendation.model.AllocationLeg;
import com.banknifty.recommendation.model.DecisionContext;
import com.banknifty.recommendation.model.MarketSummary;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.PortfolioAllocation;
import com.banknifty.recommendation.model.RankedContract;
import com.banknifty.recommendation.model.RecommendationResponse;
import com.banknifty.recommendation.model.RiskPlan;
import com.banknifty.recommendation.model.ScoreBreakdown;
import com.banknifty.recommendation.model.StrikeCandidate;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.model.TradeSetup;
import com.banknifty.recommendation.model.WinnerExplanation;
import com.banknifty.recommendation.pipeline.RecommendationPipeline;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationResponseBuilder {

	private final RecommendationAggregator recommendationAggregator;

	private final TradeSetupBuilder tradeSetupBuilder;

	private final RecommendationDecisionService decisionService;

	private final BacktestService backtestService;

	private final TradingProperties tradingProperties;

	public RecommendationResponse build(

			RecommendationContext context,

			RecommendationPipeline pipeline,

			TradeRecommendation winner) {

		boolean entryAllowed = context.isEntryAllowed();

		OptionAnalysis directionalBest = pipeline.getDirectionalBest();

		/*
		 * --------------------------------------------------------- Build Trade Setup
		 * ---------------------------------------------------------
		 */

		DecisionContext decisionContext = DecisionContext.builder()

				.request(context.getRequest())

				.indicators(context.getIndicators())

				.candles(context.getCandles())

				.marketContext(context.getMarketContext())

				.regime(context.getRegime())

				.trendAnalysis(context.getStructure())

				.build();

		TradeSetup tradeSetup = directionalBest == null

				? null

				: tradeSetupBuilder.build(

						decisionContext,

						strikeCandidate(directionalBest));

		entryAllowed = entryAllowed

				&& tradeSetup != null

				&& tradeSetup.tradable();

		/*
		 * --------------------------------------------------------- Final
		 * Recommendation ---------------------------------------------------------
		 */

		if (entryAllowed) {

			winner = decisionService.trade(

					context.getRequest(),

					context.getSpot(),

					context.getSignal(),

					context.getInstitutional(),

					directionalBest);

			backtestService.saveRecommendation(directionalBest);

		} else if (winner.action() == RecommendationAction.BUY) {

			winner = decisionService.noTrade(

					context.getRequest(),

					context.getSpot(),

					context.getSignal(),

					context.getInstitutional(),

					tradeSetup == null

							? "No directional contract matched the detected setup"

							: "Trade setup rejected: " + String.join("; ", tradeSetup.rejectedReasons()));
		}

		/*
		 * --------------------------------------------------------- Refresh Summary
		 * ---------------------------------------------------------
		 */

		MarketSummary summary = marketSummary(

				winner,

				context.getRegime(),

				context.getStructure());

		/*
		 * --------------------------------------------------------- Winner Explanation
		 * ---------------------------------------------------------
		 */

		WinnerExplanation explanation = entryAllowed

				? winnerExplanation(

						directionalBest,

						tradeSetup)

				: null;

		/*
		 * --------------------------------------------------------- Portfolio
		 * Allocation ---------------------------------------------------------
		 */

		PortfolioAllocation allocation = portfolioAllocation(

				entryAllowed

						? winner

						: null,

				pipeline.getTop(),

				context.getRequest().riskProfile());

		/*
		 * --------------------------------------------------------- Final Response
		 * ---------------------------------------------------------
		 */

		return recommendationAggregator.aggregate(

				winner,

				pipeline.getTop(),

				pipeline.getRejected(),

				summary,

				tradeSetup,

				explanation,

				allocation,

				entryAllowed

						? riskPlan(

								winner,

								context.getRequest().capital())

						: null,

				context.getPrediction(),

				context.getReversal(),

				entryAllowed

						? "Winner emitted; persist this response with outcome prices to enable backtesting and learning."

						: "No executable winner: rankings are informational until market-entry gates are met.");
	}

	/*
	 * -----------------------------------------------------------------------
	 * Helper methods (Copied from DefaultRecommendationEngine)
	 * -----------------------------------------------------------------------
	 */

	private StrikeCandidate strikeCandidate(OptionAnalysis analysis) {

		var candidate = analysis.getCandidate();

		return StrikeCandidate.builder()

				.tradingSymbol(candidate.getTradingSymbol())

				.instrumentToken(candidate.getInstrumentToken())

				.strike(candidate.getStrike())

				.optionType(candidate.getOptionType())

				.ltp(candidate.getPremium())

				.openInterest(candidate.getOpenInterest())

				.volume(candidate.getVolume())

				.iv(candidate.getIv())

				.delta(candidate.getDelta())

				.theta(candidate.getTheta())

				.gamma(candidate.getGamma())

				.vega(candidate.getVega())

				.build();
	}

	private MarketSummary marketSummary(

			TradeRecommendation recommendation,

			com.banknifty.market.regime.MarketRegimeResult regime,

			com.banknifty.service.TrendAnalysisResult structure) {

		var institutional = recommendation.institutionalAnalysis();

		return new MarketSummary(

				recommendation.spotPrice(),

				recommendation.technicalBias(),

				recommendation.technicalConfidence() == null

						? 0

						: recommendation.technicalConfidence(),

				institutional == null

						? null

						: institutional.getMarketBias(),

				institutional == null

						? 0

						: institutional.getConfidence(),

				regime == null

						? null

						: regime.regime(),

				institutional == null

						? null

						: institutional.getPutCallRatio(),

				institutional == null

						? null

						: institutional.getMaxPainStrike(),

				institutional == null

						? null

						: institutional.getStrongestSupportStrike(),

				institutional == null

						? null

						: institutional.getStrongestResistanceStrike(),

				setup(structure));
	}

	private String setup(

			com.banknifty.service.TrendAnalysisResult structure) {

		if (structure == null)

			return "UNKNOWN";

		if (structure.breakout())

			return "BREAKOUT";

		if (structure.breakdown())

			return "BREAKDOWN";

		if (structure.nearSupport() || structure.nearResistance())

			return "PULLBACK";

		return structure.action() == RecommendationAction.BUY

				? "TREND_CONTINUATION"

				: "NO_CONFIRMED_SETUP";
	}

	private WinnerExplanation winnerExplanation(OptionAnalysis winner, TradeSetup setup) {

		ScoreBreakdown score = scoreBreakdown(winner);

		List<String> highlights = new ArrayList<>();

		highlights.add("Highest eligible setup score: " + setup.safeScore());

		if (score.openInterest() >= 70) {

			highlights.add("Strong OI confirmation");
		}

		if (score.liquidity() >= 70) {

			highlights.add("High liquidity and tight execution quality");
		}

		if (score.riskReward() >= 70) {

			highlights.add("Favourable risk/reward profile");
		}

		var candidate = winner.getCandidate();

		if (candidate.getSpreadPercentage() != null && candidate.getSpreadPercentage().doubleValue() <= 1) {

			highlights.add("Low bid-ask spread");
		}

		return new WinnerExplanation(

				grade(winner.getTotalScore()),

				winner.getTotalScore(),

				List.copyOf(highlights));
	}

	private ScoreBreakdown scoreBreakdown(OptionAnalysis analysis) {

		double technical = percent(

				analysis.getTrendScore()

						+ analysis.getSupportResistanceScore()

						+ analysis.getPivotScore(),

				35);

		double setup = percent(

				analysis.getStrikeScore()

						+ analysis.getExpiryScore()

						+ analysis.getVolatilityScore(),

				25);

		return new ScoreBreakdown(

				technical,

				setup,

				percent(analysis.getOpenInterestScore(), 10),

				percent(analysis.getGreekScore(), 5),

				percent(analysis.getLiquidityScore(), 12),

				percent(analysis.getRiskRewardScore(), 5),

				analysis.getTotalScore(),

				analysis.getTrendScore(),

				analysis.getSupportResistanceScore(),

				analysis.getPivotScore(),

				analysis.getStrikeScore(),

				analysis.getExpiryScore(),

				analysis.getVolatilityScore());
	}

	private double percent(double value, double maximum) {

		return Math.round(

				Math.min(100, value * 100 / maximum)

						* 10.0)

				/ 10.0;
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

	private PortfolioAllocation portfolioAllocation(TradeRecommendation winner, List<RankedContract> top,
			RiskProfile profile) {

		if (winner == null || winner.action() != RecommendationAction.BUY) {

			return new PortfolioAllocation(List.of(), 100);
		}

		int deployed = switch (profile) {

		case CONSERVATIVE -> 40;

		case MODERATE -> 50;

		case BALANCED -> 60;

		case AGGRESSIVE -> 75;
		};

		List<RankedContract> sameDirection = top.stream()

				.filter(c -> c.optionType() == winner.optionType())

				.limit(2)

				.toList();

		if (sameDirection.isEmpty()) {

			return new PortfolioAllocation(List.of(), 100);
		}

		int first = sameDirection.size() == 1

				? deployed

				: (int) Math.round(deployed * .60);

		List<AllocationLeg> legs = new ArrayList<>();

		legs.add(

				new AllocationLeg(

						sameDirection.getFirst().tradingSymbol(),

						sameDirection.getFirst().optionType(),

						sameDirection.getFirst().strikePrice(),

						first,

						"Primary winner"));

		if (sameDirection.size() > 1) {

			RankedContract second = sameDirection.get(1);

			legs.add(

					new AllocationLeg(

							second.tradingSymbol(),

							second.optionType(),

							second.strikePrice(),

							deployed - first,

							"Secondary high-quality contract"));
		}

		return new PortfolioAllocation(

				List.copyOf(legs),

				100 - deployed);
	}

	private RiskPlan riskPlan(TradeRecommendation recommendation, Double capital) {

		if (recommendation.action() != RecommendationAction.BUY || recommendation.entryMin() == null
				|| recommendation.stopLoss() == null || recommendation.target1() == null) {

			return null;
		}

		int lots = Math.max(1, recommendation.quantity());

		BigDecimal entry = recommendation.entryMin();

		BigDecimal riskPerLot = entry

				.subtract(recommendation.stopLoss())

				.multiply(BigDecimal.valueOf(tradingProperties.getLotSize()))

				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal allocation = entry

				.multiply(BigDecimal.valueOf(tradingProperties.getLotSize()))

				.multiply(BigDecimal.valueOf(lots))

				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal loss = riskPerLot

				.multiply(BigDecimal.valueOf(lots))

				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal reward = recommendation.target1()

				.subtract(entry);

		BigDecimal ratio = entry.subtract(recommendation.stopLoss()).signum() <= 0

				? BigDecimal.ZERO

				: reward.divide(entry.subtract(recommendation.stopLoss()), 2, RoundingMode.HALF_UP);

		return new RiskPlan(

				entry,

				recommendation.stopLoss(),

				recommendation.target1(),

				recommendation.target2(),

				riskPerLot,

				allocation,

				loss,

				ratio,

				lots,

				lots * tradingProperties.getLotSize());
	}
}