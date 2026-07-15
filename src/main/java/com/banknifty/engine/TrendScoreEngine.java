package com.banknifty.engine;

import com.banknifty.indicator.EMAIndicatorEngine;
import com.banknifty.indicator.result.EMAResult;
import com.banknifty.model.Candle;
import com.banknifty.model.TrendDirection;
import com.banknifty.model.TrendScore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrendScoreEngine {

	private final EMAIndicatorEngine emaIndicatorEngine;

	public TrendScoreEngine(EMAIndicatorEngine emaIndicatorEngine) {
		this.emaIndicatorEngine = emaIndicatorEngine;
	}

	public TrendScore calculate(List<Candle> candles) {

		EMAResult ema = emaIndicatorEngine.calculate(candles);

		int score = 0;

		List<String> reasons = new ArrayList<>();

		if (ema.bullishCross()) {

			score += 40;
			reasons.add("EMA Bullish Cross");

		}

		if (ema.bearishCross()) {

			score -= 40;
			reasons.add("EMA Bearish Cross");

		}

		if (ema.rising()) {

			score += 20;
			reasons.add("EMA Rising");

		}

		if (ema.falling()) {

			score -= 20;
			reasons.add("EMA Falling");

		}

		TrendDirection direction;

		boolean buyCE = false;
		boolean buyPE = false;

		String regime;

		if (score >= 30) {

			direction = TrendDirection.BULLISH;
			regime = "TRENDING";
			buyCE = true;

		} else if (score <= -30) {

			direction = TrendDirection.BEARISH;
			regime = "TRENDING";
			buyPE = true;

		} else {

			direction = TrendDirection.SIDEWAYS;
			regime = "RANGE";

		}

		return TrendScore.builder()

				.totalScore(score)

				.confidence(Math.min(Math.abs(score), 100))

				.trendDirection(direction)

				.marketRegime(regime)

				.buyCE(buyCE)

				.buyPE(buyPE)

				.reasons(reasons)

				.build();

	}

}