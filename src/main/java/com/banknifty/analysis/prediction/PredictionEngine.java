package com.banknifty.analysis.prediction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.reversal.ReversalAnalysis;
import com.banknifty.indicator.result.ADXResult;
import com.banknifty.indicator.result.EMAResult;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.indicator.result.MACDResult;
import com.banknifty.indicator.result.RSIResult;
import com.banknifty.indicator.result.VWAPResult;

@Service
public class PredictionEngine {

	public PredictionAnalysis predict(PredictionContext context, ReversalAnalysis reversal) {

		IndicatorSnapshot indicators = context.getIndicators();

		EMAResult ema = indicators.ema();
		RSIResult rsi = indicators.rsi();
		MACDResult macd = indicators.macd();
		VWAPResult vwap = indicators.vwap();
		ADXResult adx = indicators.adx();

		double bullish = 0;
		double bearish = 0;

		List<String> reasons = new ArrayList<>();

		/*
		 * EMA
		 */
		if (ema.bullishAlignment()) {

			bullish += 25;

			reasons.add("EMA Bullish Alignment");

		} else if (ema.bearishAlignment()) {

			bearish += 25;

			reasons.add("EMA Bearish Alignment");
		}

		/*
		 * RSI
		 */
		if (rsi.bullish() && rsi.rising()) {

			bullish += 15;

			reasons.add("RSI Rising");

		} else if (rsi.bearish() && rsi.falling()) {

			bearish += 15;

			reasons.add("RSI Falling");
		}

		/*
		 * MACD
		 */
		if (macd.bullishCross()) {

			bullish += 20;

			reasons.add("MACD Bullish Cross");

		} else if (macd.bearishCross()) {

			bearish += 20;

			reasons.add("MACD Bearish Cross");

		} else if (macd.bullish()) {

			bullish += 10;

		} else if (macd.bearish()) {

			bearish += 10;
		}

		/*
		 * VWAP
		 */
		if (vwap.aboveVWAP()) {

			bullish += 10;

		} else {

			bearish += 10;
		}

		/*
		 * ADX
		 */
		if (adx.strongTrend()) {

			if (adx.bullish()) {

				bullish += 15;

				reasons.add("ADX Bullish");

			} else if (adx.bearish()) {

				bearish += 15;

				reasons.add("ADX Bearish");
			}

		} else {

			bullish += 3;
			bearish += 3;
		}

		/*
		 * Reversal Adjustment
		 */
		if (reversal != null) {

			if (reversal.bullishReversal()) {

				bullish += reversal.getReversalProbability() * 0.20;

			} else if (reversal.bearishReversal()) {

				bearish += reversal.getReversalProbability() * 0.20;
			}
		}

		PredictionDirection direction;

		double confidence;

		if (bullish > bearish) {

			direction = PredictionDirection.BULLISH;

			confidence = bullish;

		} else if (bearish > bullish) {

			direction = PredictionDirection.BEARISH;

			confidence = bearish;

		} else {

			direction = PredictionDirection.SIDEWAYS;

			confidence = 50;
		}

		confidence = Math.min(confidence, 100);

		PredictionStrength strength;

		if (confidence >= 90) {

			strength = PredictionStrength.VERY_STRONG;

		} else if (confidence >= 75) {

			strength = PredictionStrength.STRONG;

		} else if (confidence >= 60) {

			strength = PredictionStrength.MODERATE;

		} else if (confidence >= 45) {

			strength = PredictionStrength.WEAK;

		} else {

			strength = PredictionStrength.VERY_WEAK;
		}

		double continuation = reversal == null ? confidence : reversal.getContinuationProbability();

		double reversalProbability = reversal == null ? 100 - confidence : reversal.getReversalProbability();

		double expectedMove = Math.max(50, confidence * 2.5);

		return PredictionAnalysis.builder().direction(direction).strength(strength).confidence(confidence)
				.expectedMove(expectedMove).continuationProbability(continuation)
				.reversalProbability(reversalProbability).reasons(reasons).build();
	}
}