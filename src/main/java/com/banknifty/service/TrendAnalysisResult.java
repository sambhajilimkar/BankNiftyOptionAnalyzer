package com.banknifty.service;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Complete result of BANKNIFTY underlying technical analysis.
 *
 * This object represents the underlying market analysis only. Option-contract
 * selection and ranking are handled downstream.
 *
 * In addition to the existing indicator values, this result now exposes
 * structural market information required by the future Trade Setup Engine.
 */
@Builder
public record TrendAnalysisResult(

		/*
		 * ===================================================== CURRENT RECOMMENDATION
		 * BIAS =====================================================
		 */

		RecommendationAction action,

		OptionType optionType,

		BigDecimal spotPrice,

		Integer confidence,

		/*
		 * ===================================================== TECHNICAL SCORE
		 *
		 * Positive = bullish Negative = bearish
		 *
		 * This is intentionally exposed separately from confidence. Confidence measures
		 * confirmation quality while technicalScore represents directional strength.
		 * =====================================================
		 */

		Integer technicalScore,

		/*
		 * ===================================================== CORE INDICATORS
		 * =====================================================
		 */

		BigDecimal ema20,

		BigDecimal ema50,

		BigDecimal rsi,

		BigDecimal macd,

		BigDecimal adx,

		BigDecimal vwap,

		/*
		 * ===================================================== MARKET STRUCTURE
		 *
		 * These objects are already calculated by TrendAnalysisService. Exposing them
		 * here prevents downstream setup detection from recalculating the same
		 * information. =====================================================
		 */

		SupportResistanceResult supportResistance,

		PivotResult pivot,

		/*
		 * ===================================================== OPEN INTEREST STRUCTURE
		 * =====================================================
		 */

		OpenInterestAnalysisService.OpenInterestResult openInterest,

		/*
		 * ===================================================== EXPLANATION
		 * =====================================================
		 */

		List<String> reasons

) {

	/**
	 * Convenience method for downstream engines.
	 */
	public boolean bullishBias() {

		return technicalScore != null && technicalScore > 0;
	}

	/**
	 * Convenience method for downstream engines.
	 */
	public boolean bearishBias() {

		return technicalScore != null && technicalScore < 0;
	}

	/**
	 * True when price structure confirms an upside breakout.
	 */
	public boolean breakout() {

		return supportResistance != null && supportResistance.breakout();
	}

	/**
	 * True when price structure confirms a downside breakdown.
	 */
	public boolean breakdown() {

		return supportResistance != null && supportResistance.breakdown();
	}

	/**
	 * True when current price is close to calculated support.
	 */
	public boolean nearSupport() {

		return supportResistance != null && supportResistance.nearSupport();
	}

	/**
	 * True when current price is close to calculated resistance.
	 */
	public boolean nearResistance() {

		return supportResistance != null && supportResistance.nearResistance();
	}
}