package com.banknifty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

	/**
	 * Minimum option premium.
	 */
	private double minimumPremium = 50;

	/**
	 * Maximum option premium.
	 */
	private double maximumPremium = 1000;

	/**
	 * Minimum traded volume.
	 */
	private long minimumVolume = 1000;

	/**
	 * Minimum Open Interest.
	 */
	private long minimumOpenInterest = 5000;

	/**
	 * Maximum allowed bid-ask spread (%).
	 */
	private double maximumSpread = 2.5;

	/**
	 * Strike interval. BANKNIFTY = 100 NIFTY = 50
	 */
	private int strikeStep = 100;

	/**
	 * Maximum strike distance from ATM.
	 */
	private int maxStrikeDistance = 2000;

	/**
	 * Number of strikes to analyse on each side of ATM.
	 */
	private int strikesAroundATM = 15;

	/**
	 * BANKNIFTY lot size.
	 */
	private int lotSize = 30;

	// ==========================================================
	// Recommendation Configuration
	// ==========================================================

	/**
	 * Minimum technical confidence required.
	 */
	private int minimumTechnicalConfidence = 60;

	/**
	 * Minimum combined recommendation confidence.
	 */
	private int minimumRecommendationConfidence = 55;

	/**
	 * Reversal probability above which entries are blocked.
	 */
	private int maximumAllowedReversalProbability = 75;

	/**
	 * Number of contracts passed from RankingEngine to ValidationEngine.
	 */
	private int rankingCandidates = 20;

	/**
	 * Number of contracts returned in API response.
	 */
	private int topContracts = 5;

	/**
	 * Whether institutional direction mismatch should reject a trade.
	 *
	 * Recommended: false RankingEngine should apply a penalty instead of hard
	 * rejection.
	 */
	private boolean validateInstitutionalDirection = false;

}