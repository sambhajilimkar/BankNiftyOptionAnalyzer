package com.banknifty.recommendation.model;

/**
 * Trading setup detected on the BANKNIFTY underlying.
 *
 * A SetupType describes WHY a trade opportunity exists. It is deliberately
 * separate from CE / PE contract selection.
 */
public enum SetupType {

	/**
	 * Price breaks above established resistance with bullish technical
	 * confirmation.
	 */
	BULLISH_BREAKOUT,

	/**
	 * Price breaks below established support with bearish technical confirmation.
	 */
	BEARISH_BREAKDOWN,

	/**
	 * Existing bullish trend pulls back toward support, EMA or VWAP and shows
	 * continuation characteristics.
	 */
	BULLISH_PULLBACK,

	/**
	 * Existing bearish trend retraces toward resistance, EMA or VWAP and shows
	 * continuation characteristics.
	 */
	BEARISH_PULLBACK,

	/**
	 * Price moves back above VWAP after trading below it, with bullish
	 * confirmation.
	 */
	VWAP_BULLISH_RECLAIM,

	/**
	 * Price rejects VWAP from below / loses VWAP after testing it, with bearish
	 * confirmation.
	 */
	VWAP_BEARISH_REJECTION,

	/**
	 * Bullish reversal around an established support area.
	 *
	 * Kept for future implementation after candle-pattern confirmation is
	 * available.
	 */
	BULLISH_REVERSAL,

	/**
	 * Bearish reversal around an established resistance area.
	 *
	 * Kept for future implementation after candle-pattern confirmation is
	 * available.
	 */
	BEARISH_REVERSAL,

	/**
	 * No sufficiently strong trading setup was detected.
	 */
	NONE
}