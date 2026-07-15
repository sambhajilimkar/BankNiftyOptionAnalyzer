package com.banknifty.recommendation.model;

import com.banknifty.analysis.MarketBiasResult;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;
import com.banknifty.optionchain.analysis.OIAnalysisResult;
import com.banknifty.optionchain.model.OptionSnapshot;

public interface TradeScoringEngine {

	TradeScore score(

			IndicatorSnapshot indicators,

			MarketBiasResult marketBias,

			OptionSnapshot optionSnapshot,

			OIAnalysisResult oiAnalysis,

			MarketContext context

	);

}