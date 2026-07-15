package com.banknifty.strategy;

import org.springframework.stereotype.Component;

import com.banknifty.indicator.result.IndicatorSnapshot;

@Component
public class TrendStrategy {

	public int score(IndicatorSnapshot snapshot) {

		int score = 0;

		if (snapshot.ema().bullishAlignment())
			score += 25;

		if (snapshot.macd().bullishCross())
			score += 20;

		if (snapshot.vwap().priceAboveVWAP())
			score += 10;

		if (snapshot.adx().trending())
			score += 15;

		if (snapshot.atr().highVolatility())
			score += 5;

		return score;

	}

}