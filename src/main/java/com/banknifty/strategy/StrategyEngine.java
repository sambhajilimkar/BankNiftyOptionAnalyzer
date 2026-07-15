package com.banknifty.strategy;

import com.banknifty.enums.SignalType;
import com.banknifty.indicator.result.IndicatorSnapshot;

import org.springframework.stereotype.Service;

@Service
public class StrategyEngine {

	private final TrendStrategy trendStrategy;

	public StrategyEngine(TrendStrategy trendStrategy) {

		this.trendStrategy = trendStrategy;

	}

	public StrategyResult evaluate(IndicatorSnapshot snapshot) {

		int score = trendStrategy.score(snapshot);

		SignalType signal;

		if (score >= 70) {

			signal = SignalType.BUY_CE;

		} else if (score <= 25) {

			signal = SignalType.BUY_PE;

		} else {

			signal = SignalType.NO_TRADE;

		}

		return StrategyResult.builder()

				.score(score)

				.signal(signal)

				.confidence(score)

				.reason("Trend Strategy")

				.build();

	}

}