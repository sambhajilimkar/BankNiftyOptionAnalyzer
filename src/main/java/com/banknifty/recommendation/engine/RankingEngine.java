package com.banknifty.recommendation.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.enums.OptionType;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;

import lombok.extern.slf4j.Slf4j;

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

		return rank(analyses, context).stream().limit(limit).collect(Collectors.toList());
	}

	public List<OptionAnalysis> rank(List<OptionAnalysis> analyses, AnalysisContext context) {

		if (analyses == null || analyses.isEmpty()) {
			return List.of();
		}

		log.debug("Ranking {} analysed contracts", analyses.size());

		/*
		 * RankingEngine NEVER recalculates scores.
		 *
		 * Responsibilities: 1. Minor calibration 2. Tradable filtering 3. Sorting 4.
		 * Duplicate removal 5. Rank assignment
		 */

		analyses.forEach(this::calibrateScore);

		List<OptionAnalysis> ranked = analyses.stream().filter(this::isTradable).sorted(rankingComparator())
				.collect(Collectors.toList());

		ranked = removeDuplicateStrikes(ranked);

		assignRanks(ranked);

		log.info("Ranking completed. Analysed={}, Qualified={}", analyses.size(), ranked.size());

		return ranked;
	}

	private void assignRanks(List<OptionAnalysis> analyses) {

		int rank = 1;

		for (OptionAnalysis analysis : analyses) {
			analysis.setRank(rank++);
		}
	}

	private double liquidityScore(OptionAnalysis analysis) {

		if (analysis.getCandidate() == null) {
			return 0;
		}

		return analysis.getCandidate().getVolume() + analysis.getCandidate().getOpenInterest();
	}

	private Comparator<OptionAnalysis> rankingComparator() {

		return Comparator

				/*
				 * Highest Total Score
				 */
				.comparingDouble(OptionAnalysis::getTotalScore).reversed()

				/*
				 * Highest Probability
				 */
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getProbabilityScore).reversed())

				/*
				 * Highest Confidence
				 */
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getConfidence).reversed())

				/*
				 * Better Risk Reward
				 */
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getRiskRewardScore).reversed())

				/*
				 * Better Liquidity
				 */
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getLiquidityScore).reversed())

				/*
				 * Higher Open Interest
				 */
				.thenComparingLong(a -> a.getCandidate() == null ? 0L : a.getCandidate().getOpenInterest())

				/*
				 * Higher Volume
				 */
				.thenComparingLong(a -> a.getCandidate() == null ? 0L : a.getCandidate().getVolume())

				/*
				 * Lower Bid/Ask Spread
				 */
				.thenComparingDouble(
						a -> a.getCandidate() == null ? Double.MAX_VALUE : a.getCandidate().getSpread().doubleValue())

				/*
				 * Prefer Near ATM
				 */
				.thenComparingInt(a -> a.getCandidate() == null ? Integer.MAX_VALUE
						: Math.abs(a.getCandidate().getStrikeDistance()));
	}

	/**
	 * Minor ranking adjustments only.
	 *
	 * OptionAnalysisEngine is still the ONLY component responsible for calculating
	 * the original score.
	 *
	 * RankingEngine only performs lightweight calibration.
	 */
	private void calibrateScore(OptionAnalysis analysis) {

		if (analysis == null || analysis.getCandidate() == null) {
			return;
		}

		double score = analysis.getTotalScore();

		OptionCandidate candidate = analysis.getCandidate();

		/*
		 * ========================================================== Strike Position
		 * ==========================================================
		 */
		if (candidate.isAtm()) {

			score += 2.0;
			analysis.addReason("ATM Bonus");

		} else if (candidate.isItm()) {

			score += 1.0;
			analysis.addReason("ITM Bonus");

		} else if (candidate.isOtm()) {

			score -= 2.0;
			analysis.addReason("OTM Penalty");
		}

		/*
		 * ========================================================== Liquidity
		 * ==========================================================
		 */
		double liquidity = analysis.getLiquidityScore();

		if (liquidity >= 90) {

			score += 2.5;
			analysis.addReason("Excellent Liquidity");

		} else if (liquidity >= 75) {

			score += 1.5;
			analysis.addReason("High Liquidity");

		} else if (liquidity < 40) {

			score -= 3.0;
			analysis.addReason("Poor Liquidity");
		}

		/*
		 * ========================================================== Risk Reward
		 * ==========================================================
		 */
		double rr = analysis.getRiskRewardScore();

		if (rr >= 90) {

			score += 2.0;
			analysis.addReason("Excellent Risk Reward");

		} else if (rr < 60) {

			score -= 2.0;
			analysis.addReason("Weak Risk Reward");
		}

		/*
		 * ========================================================== Probability
		 * ==========================================================
		 */
		if (analysis.getProbabilityScore() >= 90) {

			score += 2.0;
			analysis.addReason("High Probability");

		} else if (analysis.getProbabilityScore() < 60) {

			score -= 2.0;
			analysis.addReason("Low Probability");
		}

		/*
		 * ========================================================== Confidence
		 * ==========================================================
		 */
		if (analysis.getConfidence() >= 90) {

			score += 2.0;
			analysis.addReason("High Confidence");

		} else if (analysis.getConfidence() < 60) {

			score -= 2.0;
			analysis.addReason("Low Confidence");
		}

		/*
		 * ========================================================== Open Interest
		 * ==========================================================
		 */
		if (candidate.getOpenInterest() > 1_000_000) {

			score += 1.5;
			analysis.addReason("Strong Open Interest");

		} else if (candidate.getOpenInterest() < 25_000) {

			score -= 2.0;
			analysis.addReason("Weak Open Interest");
		}

		/*
		 * ========================================================== Volume
		 * ==========================================================
		 */
		if (candidate.getVolume() > 100_000) {

			score += 1.5;
			analysis.addReason("High Volume");

		} else if (candidate.getVolume() < 2_000) {

			score -= 2.0;
			analysis.addReason("Low Volume");
		}

		/*
		 * ========================================================== Clamp Score
		 * ==========================================================
		 */
		score = Math.max(0.0, Math.min(100.0, score));

		analysis.setTotalScore(score);

		/*
		 * Recalculate confidence after calibration
		 */
		analysis.calculateConfidence();
	}

	private boolean isTradable(OptionAnalysis analysis) {

		if (analysis == null || analysis.getCandidate() == null) {
			return false;
		}

		OptionCandidate candidate = analysis.getCandidate();

		/*
		 * Premium Check
		 */
		if (candidate.getPremium() == null || candidate.getPremium().doubleValue() <= 0) {

			analysis.addReason("Rejected : Invalid Premium");
			return false;
		}

		/*
		 * Liquidity Check
		 */
		if (candidate.getLiquidityIndex() != null && candidate.getLiquidityIndex().doubleValue() <= 0) {

			analysis.addReason("Rejected : No Liquidity");
			return false;
		}

		/*
		 * Minimum Score
		 */
		if (analysis.getTotalScore() < 70) {

			analysis.addReason("Rejected : Low Score");
			return false;
		}

		/*
		 * Minimum Confidence
		 */
		if (analysis.getConfidence() < 65) {

			analysis.addReason("Rejected : Low Confidence");
			return false;
		}

		return true;
	}

	private List<OptionAnalysis> removeDuplicateStrikes(List<OptionAnalysis> analyses) {

		Map<String, OptionAnalysis> unique = new LinkedHashMap<>();

		for (OptionAnalysis analysis : analyses) {

			OptionCandidate candidate = analysis.getCandidate();

			if (candidate == null) {
				continue;
			}

			String key = candidate.getExpiry() + "-" + candidate.getStrike() + "-" + candidate.getOptionType();

			OptionAnalysis existing = unique.get(key);

			if (existing == null || analysis.getTotalScore() > existing.getTotalScore()) {

				unique.put(key, analysis);
			}
		}

		return new ArrayList<>(unique.values());
	}

	public List<OptionAnalysis> highProbabilityTrades(List<OptionAnalysis> analyses, AnalysisContext context,
			double minimumScore) {

		return rank(analyses, context).stream().filter(a -> a.getProbabilityScore() >= minimumScore)
				.collect(Collectors.toList());
	}

}