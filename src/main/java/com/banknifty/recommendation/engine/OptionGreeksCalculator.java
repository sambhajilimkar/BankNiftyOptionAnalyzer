package com.banknifty.recommendation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.banknifty.enums.OptionType;

/**
 * Black-Scholes approximation used when the broker provides a live premium but
 * does not provide IV or Greeks in its quote response.
 */
public final class OptionGreeksCalculator {

	private static final double RISK_FREE_RATE = 0.06;
	private static final double MIN_VOLATILITY = 0.01;
	private static final double MAX_VOLATILITY = 5.00;
	private static final int IMPLIED_VOLATILITY_ITERATIONS = 80;

	private OptionGreeksCalculator() {
	}

	public static Greeks calculate(BigDecimal spot, Integer strike, LocalDate expiry, OptionType optionType,
			BigDecimal premium) {
		if (spot == null || spot.signum() <= 0 || strike == null || strike <= 0 || expiry == null || optionType == null
				|| premium == null || premium.signum() <= 0) {
			return Greeks.unavailable();
		}

		double s = spot.doubleValue();
		double k = strike.doubleValue();
		double t = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), expiry) + 1) / 365.0;
		double marketPrice = premium.doubleValue();
		double intrinsic = optionType == OptionType.CE ? Math.max(0, s - k) : Math.max(0, k - s);
		if (marketPrice < intrinsic) {
			return Greeks.unavailable();
		}

		double volatility = impliedVolatility(s, k, t, optionType, marketPrice);
		if (volatility <= 0) {
			return Greeks.unavailable();
		}

		double sqrtT = Math.sqrt(t);
		double d1 = (Math.log(s / k) + (RISK_FREE_RATE + (volatility * volatility / 2)) * t) / (volatility * sqrtT);
		double d2 = d1 - volatility * sqrtT;
		double pdf = normalPdf(d1);
		double delta = optionType == OptionType.CE ? normalCdf(d1) : normalCdf(d1) - 1;
		double gamma = pdf / (s * volatility * sqrtT);
		double vega = (s * pdf * sqrtT) / 100.0;
		double thetaBase = -(s * pdf * volatility) / (2 * sqrtT);
		double theta = optionType == OptionType.CE
				? (thetaBase - (RISK_FREE_RATE * k * Math.exp(-RISK_FREE_RATE * t) * normalCdf(d2))) / 365.0
				: (thetaBase + (RISK_FREE_RATE * k * Math.exp(-RISK_FREE_RATE * t) * normalCdf(-d2))) / 365.0;

		return new Greeks(decimal(volatility * 100), decimal(delta), decimal(theta), decimal(gamma), decimal(vega));
	}

	private static double impliedVolatility(double spot, double strike, double time, OptionType optionType,
			double marketPrice) {
		double low = MIN_VOLATILITY;
		double high = MAX_VOLATILITY;
		if (marketPrice > blackScholes(spot, strike, time, high, optionType)) {
			return 0;
		}
		for (int i = 0; i < IMPLIED_VOLATILITY_ITERATIONS; i++) {
			double mid = (low + high) / 2;
			if (blackScholes(spot, strike, time, mid, optionType) > marketPrice) high = mid;
			else low = mid;
		}
		return (low + high) / 2;
	}

	private static double blackScholes(double spot, double strike, double time, double volatility, OptionType type) {
		double sqrtT = Math.sqrt(time);
		double d1 = (Math.log(spot / strike) + (RISK_FREE_RATE + volatility * volatility / 2) * time)
				/ (volatility * sqrtT);
		double d2 = d1 - volatility * sqrtT;
		return type == OptionType.CE ? spot * normalCdf(d1) - strike * Math.exp(-RISK_FREE_RATE * time) * normalCdf(d2)
				: strike * Math.exp(-RISK_FREE_RATE * time) * normalCdf(-d2) - spot * normalCdf(-d1);
	}

	private static double normalPdf(double x) { return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI); }
	private static double normalCdf(double x) {
		double k = 1.0 / (1.0 + 0.2316419 * Math.abs(x));
		double polynomial = k * (0.319381530 + k * (-0.356563782 + k * (1.781477937 + k * (-1.821255978 + k * 1.330274429))));
		double value = 1.0 - normalPdf(x) * polynomial;
		return x >= 0 ? value : 1.0 - value;
	}
	private static BigDecimal decimal(double value) { return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP); }

	public record Greeks(BigDecimal iv, BigDecimal delta, BigDecimal theta, BigDecimal gamma, BigDecimal vega) {
		public static Greeks unavailable() { return new Greeks(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO); }
	}
}
