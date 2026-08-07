package com.banknifty.recommendation.builder;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.enums.OptionType;
import com.banknifty.recommendation.context.RecommendationContext;
import com.banknifty.recommendation.model.Signal;

@Component
public class AnalysisContextBuilder {

	public AnalysisContext build(RecommendationContext context) {

		Signal signal = context.getSignal();

		MarketBias marketBias = marketBias(signal);

		Integer trendScore = signal.getOptionType() == OptionType.CE ? signal.getConfidence() : -signal.getConfidence();

		AnalysisContext analysisContext = AnalysisContext.builder().spotPrice(context.getSpot())
				.marketBias(marketBias).trendScore(trendScore).confidence(signal.getConfidence())

				/*
				 * Technical Indicators
				 */
				.ema(context.getIndicators().ema()).rsi(context.getIndicators().rsi())
				.macd(context.getIndicators().macd()).vwap(context.getIndicators().vwap())
				.adx(context.getIndicators().adx())

				/*
				 * Institutional
				 */
				.institutionalAnalysis(context.getInstitutional())

				/*
				 * These will be populated in Sprint-2
				 */
				.openInterest(null).supportResistance(null).pivot(null)

				.build();

		context.setAnalysisContext(analysisContext);

		return analysisContext;
	}

	private MarketBias marketBias(Signal signal) {

		if (signal == null) {
			return MarketBias.SIDEWAYS;
		}

		if (signal.getOptionType() == OptionType.CE) {
			return signal.getConfidence() >= 80 ? MarketBias.STRONG_BULLISH : MarketBias.BULLISH;
		}

		return signal.getConfidence() >= 80 ? MarketBias.STRONG_BEARISH : MarketBias.BEARISH;
	}
}