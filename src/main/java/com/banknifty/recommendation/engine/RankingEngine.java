package com.banknifty.recommendation.engine;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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

		/*
		 * RankingEngine never calculates scores.
		 *
		 * OptionAnalysisEngine is the single source of truth.
		 *
		 * RankingEngine only performs: 1. Minor score calibration 2. Tradable filtering
		 * 3. Sorting 4. Duplicate removal 5. Rank assignment
		 */

		analyses.forEach(this::calibrateScore);

		List<OptionAnalysis> ranked = analyses.stream().filter(this::isTradable).sorted(rankingComparator())
				.collect(Collectors.toList());

		ranked = removeDuplicateStrikes(ranked);

		assignRanks(ranked);

		log.info("Ranking completed. {} contracts ranked.", ranked.size());

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

				// Highest score first
				.comparingDouble(OptionAnalysis::getTotalScore).reversed()

				// Highest confidence
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getConfidence).reversed())

				// Higher probability
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getProbabilityScore).reversed())

				// Better Risk Reward
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getRiskRewardScore).reversed())

				// Better Liquidity
				.thenComparing(Comparator.comparingDouble(OptionAnalysis::getLiquidityScore).reversed());
	}

	/**
	 * Small ranking adjustments only.
	 *
	 * OptionAnalysisEngine has already calculated the score. RankingEngine MUST
	 * NEVER recompute it.
	 */
	private void calibrateScore(OptionAnalysis analysis) {

		if (analysis == null || analysis.getCandidate() == null) {
			return;
		}

		double score = analysis.getTotalScore();

		OptionCandidate candidate = analysis.getCandidate();

		/*
		 * These methods depend on your OptionCandidate class. We'll adjust them in the
		 * next step if needed.
		 */
		if (candidate.isAtm()) {

			score += 2;

			analysis.addReason("ATM Bonus");
		} else if (candidate.isItm()) {

			score += 1;

			analysis.addReason("ITM Bonus");
		} else if (candidate.isOtm()) {

			score -= 2;

			analysis.addReason("OTM Penalty");
		}

		analysis.setTotalScore(Math.min(score, 100));

		analysis.calculateConfidence();
	}

	private boolean isTradable(OptionAnalysis analysis) {

		if (analysis == null || analysis.getCandidate() == null) {
			return false;
		}

		OptionCandidate candidate = analysis.getCandidate();

		/*
		 * Premium
		 */
		if (candidate.getPremium() == null || candidate.getPremium().doubleValue() <= 0) {

			analysis.addReason("Rejected : Invalid Premium");
			return false;
		}

		/*
		 * Liquidity
		 */
		if (candidate.getLiquidityIndex() != null && candidate.getLiquidityIndex().doubleValue() <= 0) {

			analysis.addReason("Rejected : No Liquidity");
			return false;
		}

		return true;
	}

	private List<OptionAnalysis> removeDuplicateStrikes(List<OptionAnalysis> analyses) {

		Map<String, OptionAnalysis> map = new LinkedHashMap<>();

		for (OptionAnalysis analysis : analyses) {

			OptionCandidate candidate = analysis.getCandidate();

			if (candidate == null) {
				continue;
			}

			String key = candidate.getExpiry() + "-" + candidate.getStrike() + "-" + candidate.getOptionType();

			map.putIfAbsent(key, analysis);
		}

		return new ArrayList<>(map.values());
	}

	public OptionAnalysis bestCE(List<OptionAnalysis> analyses, AnalysisContext context) {

		return rank(analyses, context).stream().filter(a -> a.getCandidate() != null)
				.filter(a -> a.getCandidate().getOptionType() == OptionType.CE).findFirst().orElse(null);
	}

	public OptionAnalysis bestPE(List<OptionAnalysis> analyses, AnalysisContext context) {

		return rank(analyses, context).stream().filter(a -> a.getCandidate() != null)
				.filter(a -> a.getCandidate().getOptionType() == OptionType.PE).findFirst().orElse(null);
	}

	public List<OptionAnalysis> highProbabilityTrades(List<OptionAnalysis> analyses, AnalysisContext context,
			double minimumScore) {

		return rank(analyses, context).stream().filter(a -> a.getTotalScore() >= minimumScore)
				.collect(Collectors.toList());
	}

}
