package com.banknifty.service.impl;

import com.banknifty.enums.RecommendationAction;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.service.TradingRuleEngine;
import org.springframework.stereotype.Service;

@Service
public class TradingRuleEngineImpl implements TradingRuleEngine {

	@Override
	public RecommendationAction evaluate(IndicatorSnapshot s) {

		if (s == null) {
			return RecommendationAction.WAIT;
		}

		/*
		 * Rule 1 Strong Bullish Trend
		 */
		if (s.ema() != null && s.rsi() != null && s.macd() != null && s.vwap() != null && s.adx() != null
				&& s.ema().bullishAlignment() && s.macd().bullishCross() && s.rsi().bullish() && s.vwap().aboveVWAP()
				&& s.adx().strongTrend()) {

			return RecommendationAction.BUY;
		}

		/*
		 * Rule 2 Strong Bearish Trend
		 */
		if (s.ema() != null && s.rsi() != null && s.macd() != null && s.vwap() != null && s.adx() != null
				&& s.ema().bearishAlignment() && s.macd().bearishCross() && s.rsi().bearish() && s.vwap().belowVWAP()
				&& s.adx().strongTrend()) {

			return RecommendationAction.BUY;
		}

		/*
		 * Rule 3 No Trade
		 */
		return RecommendationAction.WAIT;
	}

}