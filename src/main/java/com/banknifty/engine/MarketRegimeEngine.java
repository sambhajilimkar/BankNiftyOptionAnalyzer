package com.banknifty.engine;

import com.banknifty.model.Candle;
import com.banknifty.model.TrendDirection;
import com.banknifty.model.TrendScore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Determines current market regime from TrendScore.
 */
@Component
public class MarketRegimeEngine {

	private final TrendScoreEngine trendScoreEngine;

	public MarketRegimeEngine(TrendScoreEngine trendScoreEngine) {
		this.trendScoreEngine = trendScoreEngine;
	}

	public String determine(List<Candle> candles) {

		TrendScore score = trendScoreEngine.calculate(candles);

		if (score.trendDirection() == TrendDirection.BULLISH && score.confidence() >= 70) {
			return "TRENDING_BULLISH";
		}

		if (score.trendDirection() == TrendDirection.BEARISH && score.confidence() >= 70) {
			return "TRENDING_BEARISH";
		}

		if (score.confidence() >= 50) {
			return "WEAK_TREND";
		}

		return "SIDEWAYS";
	}

	public boolean isTrending(List<Candle> candles) {
		return determine(candles).startsWith("TRENDING");
	}

	public boolean isSideways(List<Candle> candles) {
		return "SIDEWAYS".equals(determine(candles));
	}
}
