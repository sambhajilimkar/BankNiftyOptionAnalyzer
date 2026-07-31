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

	private final OptionChainQualityCalculator optionChainQualityCalculator;

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

		optionChainQualityCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ MARKET SENTIMENT
		 * ============================================================
		 */
		optionChainSentimentCalculator.calculate(currentSnapshot, analysis);

		/*
		 * The sentiment calculator has already set confidence from the balance of
		 * bullish versus bearish evidence. Do not overwrite it with market-quality
		 * measures, because a liquid/high-IV market can still be directionally unclear.
		 */
		analysis.setInstitutionalScore(analysis.getConfidence());
		double qualityScore = (analysis.getMaxPainScore() + analysis.getGammaExposureScore()
				+ analysis.getSupportResistanceScore() + analysis.getLiquidityScore()
				+ analysis.getVolatilityScore()) / 5.0;
		analysis.setMarketQualityScore(qualityScore);

		log.info("Institutional directional confidence: {}, market quality: {}", analysis.getConfidence(),
				qualityScore);

		return analysis;
	}

}
