package com.banknifty.recommendation.probability;

/**
 * Probability Engine Weights.
 *
 * Total = 100
 */
public final class ProbabilityWeights {

	private ProbabilityWeights() {
	}

	public static final double TREND = 25.0;

	public static final double INSTITUTIONAL = 20.0;

	public static final double OPEN_INTEREST = 15.0;

	public static final double LIQUIDITY = 15.0;

	public static final double GREEKS = 10.0;

	public static final double RISK_REWARD = 10.0;

	public static final double VOLATILITY = 5.0;

}