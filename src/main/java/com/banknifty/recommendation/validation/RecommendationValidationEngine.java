package com.banknifty.recommendation.validation;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.config.TradingProperties;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationValidationEngine {

	private final TradingProperties tradingProperties;

	public RecommendationValidationResult validate(AnalysisContext context, OptionAnalysis analysis) {

		RecommendationValidationResult result = RecommendationValidationResult.builder().build();

		if (analysis == null || analysis.getCandidate() == null) {
			result.reject("Analysis unavailable");
			return result;
		}

		OptionCandidate candidate = analysis.getCandidate();

		/*
		 * Premium
		 */
		if (candidate.getPremium() == null) {
			result.reject("Premium unavailable");
		} else {

			double premium = candidate.getPremium().doubleValue();

			if (premium < tradingProperties.getMinimumPremium()) {

				result.reject(String.format("Premium %.2f below minimum %.2f", premium,
						tradingProperties.getMinimumPremium()));
			}

			if (premium > tradingProperties.getMaximumPremium()) {

				result.reject(String.format("Premium %.2f above maximum %.2f", premium,
						tradingProperties.getMaximumPremium()));
			}
		}

		/*
		 * Volume
		 */
		if (candidate.getVolume() < tradingProperties.getMinimumVolume()) {

			result.reject(String.format("Volume %d below minimum %d", candidate.getVolume(),
					tradingProperties.getMinimumVolume()));
		}

		/*
		 * Open Interest
		 */
		if (candidate.getOpenInterest() < tradingProperties.getMinimumOpenInterest()) {

			result.reject(String.format("Open Interest %d below minimum %d", candidate.getOpenInterest(),
					tradingProperties.getMinimumOpenInterest()));
		}

		/*
		 * Bid / Ask Spread
		 */
		if (candidate.getSpreadPercentage() != null
				&& candidate.getSpreadPercentage().doubleValue() > tradingProperties.getMaximumSpread()) {

			result.reject(String.format("Spread %.2f%% exceeds %.2f%%", candidate.getSpreadPercentage().doubleValue(),
					tradingProperties.getMaximumSpread()));
		}

		/*
		 * Institutional Direction
		 *
		 * Optional. Normally disabled.
		 */
		if (tradingProperties.isValidateInstitutionalDirection() && context != null
				&& context.getInstitutionalAnalysis() != null
				&& context.getInstitutionalAnalysis().getMarketBias() != null && context.getMarketBias() != null
				&& !sameDirection(context.getMarketBias(), context.getInstitutionalAnalysis().getMarketBias())) {

			result.reject("Technical and Institutional direction mismatch");
		}

		if (!result.hasErrors()) {
			result.approve();
		}

		return result;
	}

	private boolean sameDirection(MarketBias technical, MarketBias institutional) {

		boolean technicalBull = technical == MarketBias.BULLISH || technical == MarketBias.STRONG_BULLISH;

		boolean institutionalBull = institutional == MarketBias.BULLISH || institutional == MarketBias.STRONG_BULLISH;

		boolean technicalBear = technical == MarketBias.BEARISH || technical == MarketBias.STRONG_BEARISH;

		boolean institutionalBear = institutional == MarketBias.BEARISH || institutional == MarketBias.STRONG_BEARISH;

		return (technicalBull && institutionalBull) || (technicalBear && institutionalBear);
	}
}