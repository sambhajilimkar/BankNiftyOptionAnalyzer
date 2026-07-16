package com.banknifty.recommendation.mapper;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.recommendation.model.OptionCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OptionCandidateMapper {

	public OptionCandidate map(OptionQuote quote, BigDecimal spotPrice) {

		BigDecimal strike = BigDecimal.valueOf(quote.strike());

		BigDecimal distance = strike.subtract(spotPrice).abs();

		BigDecimal distancePercent = BigDecimal.ZERO;

		if (spotPrice != null && spotPrice.signum() > 0) {

			distancePercent = distance.multiply(BigDecimal.valueOf(100)).divide(spotPrice, 2, RoundingMode.HALF_UP);
		}

		BigDecimal spread = BigDecimal.ZERO;

		if (quote.bid() != null && quote.ask() != null) {

			spread = quote.ask().subtract(quote.bid()).abs();
		}

		BigDecimal spreadPercent = BigDecimal.ZERO;

		if (quote.ltp() != null && quote.ltp().signum() > 0) {

			spreadPercent = spread.multiply(BigDecimal.valueOf(100)).divide(quote.ltp(), 2, RoundingMode.HALF_UP);
		}

		return OptionCandidate.builder()

				/*
				 * Broker
				 */
				.instrumentToken(quote.instrumentToken()).tradingSymbol(quote.tradingSymbol()).strike(quote.strike())
				.expiry(quote.expiry()).optionType(quote.optionType()).premium(quote.ltp())

				/*
				 * Market
				 */
				.spotPrice(spotPrice).distanceFromATM(distance).distancePercent(distancePercent)

				/*
				 * Liquidity
				 */
				.volume(quote.volume()).openInterest(quote.openInterest()).bid(quote.bid()).ask(quote.ask())
				.spread(spread).spreadPercentage(spreadPercent)

				/*
				 * Greeks
				 */
				.iv(quote.iv()).delta(quote.delta()).theta(quote.theta()).gamma(quote.gamma()).vega(quote.vega())

				.build();

	}

}