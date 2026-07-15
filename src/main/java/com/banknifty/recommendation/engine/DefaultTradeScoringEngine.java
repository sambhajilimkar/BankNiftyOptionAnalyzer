package com.banknifty.recommendation.engine;

import com.banknifty.analysis.MarketBiasResult;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;
import com.banknifty.optionchain.analysis.OIAnalysisResult;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.TradeScore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultTradeScoringEngine implements TradeScoringEngine {

	@Override
	public TradeScore score(

			IndicatorSnapshot indicators,

			MarketBiasResult bias,

			OptionSnapshot optionSnapshot,

			OIAnalysisResult oi,

			MarketContext context) {

		int score = 0;

		List<String> reasons = new ArrayList<>();

		/*
		 * Market Bias
		 */
		switch (bias.bias()) {

		case STRONG_BULLISH -> {

			score += 30;
			reasons.add("Strong Bullish Trend");

		}

		case BULLISH -> {

			score += 20;
			reasons.add("Bullish Trend");

		}

		case STRONG_BEARISH -> {

			score -= 30;
			reasons.add("Strong Bearish Trend");

		}

		case BEARISH -> {

			score -= 20;
			reasons.add("Bearish Trend");

		}

		default -> reasons.add("Sideways Market");

		}

		/*
		 * OI
		 */
		if (oi.putWriting()) {

			score += 15;

			reasons.add("Put Writing");

		}

		if (oi.callWriting()) {

			score -= 15;

			reasons.add("Call Writing");

		}

		/*
		 * Trade Allowed
		 */
		if (!context.tradeAllowed()) {

			score -= 50;

			reasons.add("Market Context Restriction");

		}

		return TradeScore.builder()

				.trendScore(score)

				.momentumScore(0)

				.optionChainScore(0)

				.oiScore(0)

				.marketScore(0)

				.riskScore(0)

				.totalScore(Math.max(Math.min(score, 100), 0))

				.reasons(reasons)

				.build();

	}

}