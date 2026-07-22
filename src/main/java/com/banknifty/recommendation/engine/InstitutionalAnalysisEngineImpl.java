package com.banknifty.recommendation.engine;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionalAnalysisEngineImpl implements InstitutionalAnalysisEngine {

	private final PCRCalculator pcrCalculator;

	private final MaxPainCalculator maxPainCalculator;

	private final OIBuildupCalculator oiBuildupCalculator;

	private final GammaExposureCalculator gammaExposureCalculator;

	private final SupportResistanceCalculator supportResistanceCalculator;

	private final OptionChainSentimentCalculator optionChainSentimentCalculator;

	@Override
	public InstitutionalAnalysis analyze(OptionSnapshot currentSnapshot, OptionSnapshot previousSnapshot,
			AnalysisContext context) {

		if (currentSnapshot == null || currentSnapshot.calls() == null || currentSnapshot.puts() == null
				|| (currentSnapshot.calls().isEmpty() && currentSnapshot.puts().isEmpty())) {

			return InstitutionalAnalysis.builder().confidence(0).institutionalScore(0).build();
		}

		InstitutionalAnalysis analysis = InstitutionalAnalysis.builder().build();

		/*
		 * ============================================================ PCR
		 * ============================================================
		 */
		pcrCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ MAX PAIN
		 * ============================================================
		 */
		maxPainCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ OI BUILDUP
		 * ============================================================
		 */
		oiBuildupCalculator.calculate(previousSnapshot, currentSnapshot, analysis);

		/*
		 * ============================================================ GAMMA EXPOSURE
		 * ============================================================
		 */
		gammaExposureCalculator.calculate(currentSnapshot, analysis);

		supportResistanceCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ MARKET SENTIMENT
		 * ============================================================
		 */
		optionChainSentimentCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ FINAL SCORE
		 * ============================================================
		 */

		double score = 0;

		score += analysis.getPcrScore();
		score += analysis.getOiScore();
		score += analysis.getMaxPainScore();
		score += analysis.getGammaExposureScore();
		score += analysis.getSupportResistanceScore();
		score += analysis.getLiquidityScore();
		score += analysis.getVolatilityScore();

		score = score / 7.0;

		analysis.setInstitutionalScore(score);
		analysis.setConfidence(score);

		log.info("Institutional Score : {}", score);

		return analysis;
	}

}
