package com.banknifty.engine;

import com.banknifty.enums.SignalType;
import com.banknifty.model.OptionRecommendation;
import com.banknifty.model.TrendScore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class RecommendationEngine {

	public OptionRecommendation recommend(TrendScore trendScore) {

		if (trendScore.buyCE()) {

			return OptionRecommendation.builder()

					.signal(SignalType.BUY_CE)

					.optionType("CE")

					.confidence(trendScore.confidence())

					.reason(String.join(", ", trendScore.reasons()))

					.generatedAt(LocalDateTime.now())

					.build();

		}

		if (trendScore.buyPE()) {

			return OptionRecommendation.builder()

					.signal(SignalType.BUY_PE)

					.optionType("PE")

					.confidence(trendScore.confidence())

					.reason(String.join(", ", trendScore.reasons()))

					.generatedAt(LocalDateTime.now())

					.build();

		}

		return OptionRecommendation.builder()

				.signal(SignalType.NO_TRADE)

				.confidence(trendScore.confidence())

				.reason("No clear trading opportunity")

				.generatedAt(LocalDateTime.now())

				.build();

	}

	public OptionRecommendation enrich(

			OptionRecommendation recommendation,

			String tradingSymbol,

			Long instrumentToken,

			Integer strike,

			BigDecimal entryPrice,

			BigDecimal stopLoss,

			BigDecimal target) {

		return recommendation.toBuilder()

				.tradingSymbol(tradingSymbol)

				.instrumentToken(instrumentToken)

				.strike(strike)

				.entryPrice(entryPrice)

				.stopLoss(stopLoss)

				.target(target)

				.build();

	}

}