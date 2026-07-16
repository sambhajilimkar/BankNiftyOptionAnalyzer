package com.banknifty.recommendation.engine;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.enums.OptionType;
import com.banknifty.recommendation.model.OptionAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RankingEngine {

	public OptionAnalysis best(List<OptionAnalysis> analyses, AnalysisContext context) {

		List<OptionAnalysis> ranked = rank(analyses, context);

		if (ranked.isEmpty()) {

			return null;

		}

		return ranked.getFirst();

	}

	public List<OptionAnalysis> top(List<OptionAnalysis> analyses, AnalysisContext context, int limit) {

		return rank(analyses, context)

				.stream()

				.limit(limit)

				.collect(Collectors.toList());

	}

	private void assignRanks(List<OptionAnalysis> analyses) {

		int rank = 1;

		for (OptionAnalysis analysis : analyses) {

			analysis.setRank(rank++);

		}

	}

	private double compositeScore(OptionAnalysis analysis, AnalysisContext context) {

		double score = 0;

		score += trendWeight(analysis);

		score += liquidityWeight(analysis);

		score += strikeWeight(analysis);

		score += openInterestWeight(analysis);

		score += volatilityWeight(analysis);

		score += greekWeight(analysis);

		score += expiryWeight(analysis);

		score += riskRewardWeight(analysis);

		score -= penaltyScore(analysis, context);

		return Math.max(score, 0);

	}

	private double trendWeight(OptionAnalysis analysis) {

		return analysis.getTrendScore() * 0.35;

	}

	private double liquidityWeight(OptionAnalysis analysis) {

		return analysis.getLiquidityScore() * 0.15;

	}

	private double strikeWeight(OptionAnalysis analysis) {

		return analysis.getStrikeScore() * 0.10;

	}

	private double openInterestWeight(OptionAnalysis analysis) {

		return analysis.getOpenInterestScore() * 0.10;

	}

	private double volatilityWeight(OptionAnalysis analysis) {

		return analysis.getVolatilityScore() * 0.05;

	}

	private double greekWeight(OptionAnalysis analysis) {

		return analysis.getGreekScore() * 0.10;

	}

	private double expiryWeight(OptionAnalysis analysis) {

		return analysis.getExpiryScore() * 0.05;

	}

	private double riskRewardWeight(OptionAnalysis analysis) {

		return analysis.getRiskRewardScore() * 0.10;

	}

	private double penaltyScore(OptionAnalysis analysis, AnalysisContext context) {

		double penalty = 0;

		penalty += marketBiasPenalty(analysis, context);

		penalty += liquidityPenalty(analysis);

		penalty += strikePenalty(analysis);

		penalty += expiryPenalty(analysis);

		penalty += confidencePenalty(analysis);

		return penalty;

	}

	private double marketBiasPenalty(OptionAnalysis analysis, AnalysisContext context) {

		if (context.getMarketBias() == null) {
			return 0;
		}

		OptionType type = analysis.getCandidate().getOptionType();

		switch (context.getMarketBias()) {

		case STRONG_BULLISH:

			if (type == OptionType.PE) {
				analysis.addReason("Penalty : Against Strong Bullish Trend");
				return 20;
			}

			break;

		case BULLISH:

			if (type == OptionType.PE) {
				analysis.addReason("Penalty : Against Bullish Trend");
				return 12;
			}

			break;

		case STRONG_BEARISH:

			if (type == OptionType.CE) {
				analysis.addReason("Penalty : Against Strong Bearish Trend");
				return 20;
			}

			break;

		case BEARISH:

			if (type == OptionType.CE) {
				analysis.addReason("Penalty : Against Bearish Trend");
				return 12;
			}

			break;

		default:
			return 0;
		}

		return 0;

	}

	private double liquidityPenalty(OptionAnalysis analysis) {

		double penalty = 0;

		if (analysis.getCandidate().getLiquidityIndex() != null) {

			double liquidity = analysis.getCandidate().getLiquidityIndex().doubleValue();

			if (liquidity < 3) {

				penalty += 10;

				analysis.addReason("Penalty : Low Liquidity");

			}

		}

		if (analysis.getCandidate().getSpreadPercentage() != null) {

			double spread = analysis.getCandidate().getSpreadPercentage().doubleValue();

			if (spread > 2.5) {

				penalty += 8;

				analysis.addReason("Penalty : Wide Spread");

			}

		}

		return penalty;

	}

	private double strikePenalty(OptionAnalysis analysis) {

		OptionType type = analysis.getCandidate().getOptionType();

		if (analysis.getCandidate().isOtm()) {

			analysis.addReason("Penalty : OTM Strike");

			return 5;

		}

		return 0;

	}

	private double expiryPenalty(OptionAnalysis analysis) {

		if (analysis.getCandidate().getExpiry() == null) {

			return 0;

		}

		long days = java.time.temporal.ChronoUnit.DAYS.between(

				java.time.LocalDate.now(),

				analysis.getCandidate().getExpiry());

		if (days == 0) {

			analysis.addReason("Penalty : Expiry Day");

			return 5;

		}

		return 0;

	}

	private double confidencePenalty(OptionAnalysis analysis) {

		if (analysis.getTrendScore() < 15) {

			analysis.addReason("Penalty : Weak Trend");

			return 5;

		}

		return 0;

	}

	public List<OptionAnalysis> rank(List<OptionAnalysis> analyses, AnalysisContext context) {

		if (analyses == null || analyses.isEmpty()) {
			return List.of();
		}

		analyses.forEach(a -> {

			double composite = compositeScore(a, context);

			composite = calibrateScore(a, composite);

			a.setTotalScore(composite);

			a.setConfidence(Math.min(composite, 100));

		});

		List<OptionAnalysis> ranked = analyses.stream()

				.filter(this::isTradable)

				.sorted(Comparator.comparingDouble(OptionAnalysis::getTotalScore)

						.reversed()

						.thenComparingDouble(OptionAnalysis::getConfidence)

						.reversed())

				.collect(Collectors.toList());

		ranked = removeDuplicateStrikes(ranked);

		assignRanks(ranked);

		return ranked;

	}

	private double calibrateScore(OptionAnalysis analysis, double score) {

		if (analysis.getCandidate().isAtm()) {

			score += 3;

			analysis.addReason("ATM Bonus");

		}

		if (analysis.getCandidate().isItm()) {

			score += 2;

			analysis.addReason("ITM Bonus");

		}

		if (analysis.getCandidate().isOtm()) {

			score -= 3;

			analysis.addReason("OTM Penalty");

		}

		return Math.max(score, 0);

	}

	private boolean isTradable(OptionAnalysis analysis) {

		if (analysis.getCandidate().getPremium() == null) {

			return false;

		}

		if (analysis.getCandidate().getPremium().doubleValue() <= 0) {

			return false;

		}

		if (analysis.getCandidate().getVolume() == null) {

			return false;

		}

		if (analysis.getCandidate().getVolume() < 100L) {

			return false;

		}

		if (analysis.getCandidate().getOpenInterest() == null) {

			return false;

		}

		return analysis.getCandidate().getOpenInterest() > 500L;

	}

	private List<OptionAnalysis> removeDuplicateStrikes(List<OptionAnalysis> analyses) {

		return analyses.stream()

				.collect(Collectors.toMap(

						a -> a.getCandidate().getTradingSymbol(),

						a -> a,

						(a, b) -> a.getTotalScore() >= b.getTotalScore() ? a : b))

				.values()

				.stream()

				.sorted(Comparator.comparingDouble(OptionAnalysis::getTotalScore)

						.reversed())

				.collect(Collectors.toList());

	}

	public List<OptionAnalysis> highProbabilityTrades(

			List<OptionAnalysis> analyses,

			AnalysisContext context) {

		return rank(analyses, context)

				.stream()

				.filter(a -> a.getConfidence() >= 75)

				.collect(Collectors.toList());

	}

	public OptionAnalysis bestCE(List<OptionAnalysis> analyses, AnalysisContext context) {

		return rank(analyses, context)

				.stream()

				.filter(a -> a.getCandidate().getOptionType() == OptionType.CE)

				.findFirst()

				.orElse(null);

	}

	public OptionAnalysis bestPE(List<OptionAnalysis> analyses, AnalysisContext context) {

		return rank(analyses, context)

				.stream()

				.filter(a -> a.getCandidate().getOptionType() == OptionType.PE)

				.findFirst()

				.orElse(null);

	}

}