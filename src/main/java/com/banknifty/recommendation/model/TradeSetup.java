package com.banknifty.recommendation.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Final evaluated trade setup before recommendation.
 *
 * This object represents BOTH:
 *
 * 1. The detected underlying market setup 2. The selected option contract
 *
 * It is intentionally independent from the Recommendation DTO.
 */
@Builder
public record TradeSetup(

		/*
		 * ===================================================== SETUP INFORMATION
		 * =====================================================
		 */

		SetupType setupType,

		boolean valid,

		Integer setupScore,

		Integer confidence,

		/*
		 * ===================================================== OPTION CONTRACT
		 * =====================================================
		 */

		String instrument,

		String tradingSymbol,

		String expiry,

		Integer strike,

		String optionType,

		/*
		 * ===================================================== PRICES
		 * =====================================================
		 */

		BigDecimal spotPrice,

		BigDecimal optionPrice,

		BigDecimal entry,

		BigDecimal stopLoss,

		BigDecimal target1,

		BigDecimal target2,

		BigDecimal target3,

		/*
		 * ===================================================== RISK
		 * =====================================================
		 */

		BigDecimal riskReward,

		/*
		 * ===================================================== EXPLANATION
		 * =====================================================
		 */

		List<String> reasons,

		List<String> rejectedReasons

) {

	/**
	 * Valid BUY setup.
	 */
	public boolean tradable() {

		return valid && setupType != SetupType.NONE;
	}

	/**
	 * Bullish setup.
	 */
	public boolean bullish() {

		return switch (setupType) {

		case BULLISH_BREAKOUT, BULLISH_PULLBACK, VWAP_BULLISH_RECLAIM, BULLISH_REVERSAL -> true;

		default -> false;
		};
	}

	/**
	 * Bearish setup.
	 */
	public boolean bearish() {

		return switch (setupType) {

		case BEARISH_BREAKDOWN, BEARISH_PULLBACK, VWAP_BEARISH_REJECTION, BEARISH_REVERSAL -> true;

		default -> false;
		};
	}

	/**
	 * Convenience method.
	 */
	public boolean breakout() {

		return setupType == SetupType.BULLISH_BREAKOUT || setupType == SetupType.BEARISH_BREAKDOWN;
	}

	/**
	 * Convenience method.
	 */
	public boolean pullback() {

		return setupType == SetupType.BULLISH_PULLBACK || setupType == SetupType.BEARISH_PULLBACK;
	}

	/**
	 * Convenience method.
	 */
	public boolean vwapSetup() {

		return setupType == SetupType.VWAP_BULLISH_RECLAIM || setupType == SetupType.VWAP_BEARISH_REJECTION;
	}

	/**
	 * Convenience method.
	 */
	public boolean reversal() {

		return setupType == SetupType.BULLISH_REVERSAL || setupType == SetupType.BEARISH_REVERSAL;
	}

	/**
	 * Safe confidence.
	 */
	public int safeConfidence() {

		return confidence == null ? 0 : confidence;
	}

	/**
	 * Safe setup score.
	 */
	public int safeScore() {

		return setupScore == null ? 0 : setupScore;
	}
}