package com.banknifty.recommendation.engine;

import org.springframework.stereotype.Service;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionChainAnalyticsEngineImpl implements OptionChainAnalyticsEngine {

	private final PCRCalculator pcrCalculator;

	private final OIBuildupCalculator oiBuildupCalculator;

	private final MaxPainCalculator maxPainCalculator;

	private final GammaExposureCalculator gammaExposureCalculator;

	private final SupportResistanceCalculator supportResistanceCalculator;

	private final MarketSentimentCalculator marketSentimentCalculator;

	@Override
	public InstitutionalAnalysis analyze(OptionSnapshot previousSnapshot, OptionSnapshot currentSnapshot) {

		InstitutionalAnalysis analysis = InstitutionalAnalysis.builder().build();

		if (currentSnapshot == null) {
			return analysis;
		}

		/*
		 * ============================================================ Step-1 PCR
		 * ============================================================
		 */
		pcrCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ Step-2 OI
		 * Build-up ============================================================
		 */
		if (previousSnapshot != null) {

			oiBuildupCalculator.calculate(previousSnapshot, currentSnapshot, analysis);
		}

		/*
		 * ============================================================ Step-3 Max Pain
		 * ============================================================
		 */
		maxPainCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ Step-4 Gamma
		 * Exposure ============================================================
		 */
		gammaExposureCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ Step-5
		 * Institutional Support / Resistance
		 * ============================================================
		 */
		supportResistanceCalculator.calculate(currentSnapshot, analysis);

		/*
		 * ============================================================ Step-6 Final
		 * Institutional Sentiment
		 * ============================================================
		 */
		marketSentimentCalculator.calculate(analysis);

		log.info("""
				===========================================================
				OPTION CHAIN ANALYTICS
				===========================================================
				PCR                 : {}
				OI Score            : {}
				Max Pain            : {}
				Gamma Score         : {}
				Support             : {}
				Resistance          : {}
				Institutional Score : {}
				Confidence          : {}
				Market Bias         : {}
				===========================================================
				""", analysis.getPutCallRatio(), analysis.getOiScore(), analysis.getMaxPainStrike(),
				analysis.getGammaExposureScore(), analysis.getStrongestSupportStrike(),
				analysis.getStrongestResistanceStrike(), analysis.getInstitutionalScore(), analysis.getConfidence(),
				analysis.getMarketBias());

		return analysis;
	}

}