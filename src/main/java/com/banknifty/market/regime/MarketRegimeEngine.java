package com.banknifty.market.regime;

import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;

public interface MarketRegimeEngine {

	MarketRegimeResult detect(

			IndicatorSnapshot indicators,

			MarketContext marketContext

	);

}