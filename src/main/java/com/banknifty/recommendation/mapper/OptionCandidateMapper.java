package com.banknifty.recommendation.mapper;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.recommendation.model.OptionCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class OptionCandidateMapper {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final BigDecimal MAX_LIQUIDITY_SCORE = BigDecimal.TEN;

	private final TradingProperties tradingProperties;

	public OptionCandidate map(OptionQuote quote, BigDecimal spotPrice) {

		BigDecimal strike = BigDecimal.valueOf(quote.strike());
		BigDecimal distance = strike.subtract(spotPrice).abs();
		BigDecimal distancePercent = percentage(distance, spotPrice);
		BigDecimal spread = quote.ask().subtract(quote.bid()).abs();
		BigDecimal spreadPercentage = percentage(spread, quote.ltp());

		return OptionCandidate.builder().instrumentToken(quote.instrumentToken()).tradingSymbol(quote.tradingSymbol())
				.strike(quote.strike()).expiry(quote.expiry()).optionType(quote.optionType()).premium(quote.ltp())
				.spotPrice(spotPrice).distanceFromATM(distance).distancePercent(distancePercent).volume(quote.volume())
				.openInterest(quote.openInterest()).bid(quote.bid()).ask(quote.ask()).spread(spread)
				.spreadPercentage(spreadPercentage).liquidityIndex(liquidityIndex(quote, spreadPercentage))
				.iv(quote.iv()).delta(quote.delta()).theta(quote.theta()).gamma(quote.gamma()).vega(quote.vega())
				.build();
	}

	private BigDecimal percentage(BigDecimal value, BigDecimal base) {

		if (base == null || base.signum() <= 0) {
			return BigDecimal.ZERO;
		}

		return value.multiply(ONE_HUNDRED).divide(base, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal liquidityIndex(OptionQuote quote, BigDecimal spreadPercentage) {

		double volumeScore = normalizedScore(quote.volume(), tradingProperties.getMinimumVolume(), 4.0);
		double openInterestScore = normalizedScore(quote.openInterest(), tradingProperties.getMinimumOpenInterest(),
				4.0);
		double spreadScore = spreadScore(spreadPercentage);

		return BigDecimal
				.valueOf(Math.min(MAX_LIQUIDITY_SCORE.doubleValue(), volumeScore + openInterestScore + spreadScore))
				.setScale(2, RoundingMode.HALF_UP);
	}

	private double normalizedScore(Long actual, long threshold, double maximumScore) {

		if (actual == null || actual <= 0 || threshold <= 0) {
			return 0;
		}

		return Math.min(maximumScore, actual.doubleValue() / threshold * maximumScore);
	}

	private double spreadScore(BigDecimal spreadPercentage) {

		if (spreadPercentage == null) {
			return 0;
		}

		double maximumSpreadPercentage = tradingProperties.getMaximumSpread();

		if (maximumSpreadPercentage <= 0 || spreadPercentage.doubleValue() > maximumSpreadPercentage) {
			return 0;
		}

		return 2.0 * (1.0 - (spreadPercentage.doubleValue() / maximumSpreadPercentage));
	}
}
