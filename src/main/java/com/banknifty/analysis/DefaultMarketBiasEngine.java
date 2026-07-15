package com.banknifty.analysis;

import com.banknifty.indicator.result.IndicatorSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DefaultMarketBiasEngine implements MarketBiasEngine {

	@Override
	public MarketBiasResult analyse(IndicatorSnapshot snapshot) {

		int score = 0;

		List<String> reasons = new ArrayList<>();

		/*
		 * EMA
		 */
		if (snapshot.ema().bullishAlignment()) {

			score += 30;
			reasons.add("EMA Bullish Alignment");

		}

		if (snapshot.ema().bearishAlignment()) {

			score -= 30;
			reasons.add("EMA Bearish Alignment");

		}

		/*
		 * RSI
		 */
		if (snapshot.rsi().bullish()) {

			score += 15;
			reasons.add("RSI Bullish");

		}

		if (snapshot.rsi().bearish()) {

			score -= 15;
			reasons.add("RSI Bearish");

		}

		/*
		 * MACD
		 */
		if (snapshot.macd().bullishCross()) {

			score += 20;
			reasons.add("MACD Bullish");

		}

		if (snapshot.macd().bearishCross()) {

			score -= 20;
			reasons.add("MACD Bearish");

		}

		/*
		 * VWAP
		 */
		if (snapshot.vwap().aboveVWAP()) {

			score += 10;
			reasons.add("Above VWAP");

		}

		if (snapshot.vwap().belowVWAP()) {

			score -= 10;
			reasons.add("Below VWAP");

		}

		/*
		 * ADX
		 */
		if (snapshot.adx().strongTrend()) {

			score += 10;
			reasons.add("Strong Trend");

		}

		MarketBias bias;

		if (score >= 60) {

			bias = MarketBias.STRONG_BULLISH;

		} else if (score >= 25) {

			bias = MarketBias.BULLISH;

		} else if (score <= -60) {

			bias = MarketBias.STRONG_BEARISH;

		} else if (score <= -25) {

			bias = MarketBias.BEARISH;

		} else {

			bias = MarketBias.SIDEWAYS;

		}

		return MarketBiasResult.builder().bias(bias).confidence(Math.min(Math.abs(score), 100)).reasons(reasons)
				.build();
	}

}