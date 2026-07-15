package com.banknifty.analysis;

import com.banknifty.indicator.result.IndicatorSnapshot;

public interface MarketBiasEngine {

	MarketBiasResult analyse(IndicatorSnapshot snapshot);

}