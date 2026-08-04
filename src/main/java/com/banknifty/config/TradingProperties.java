package com.banknifty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
	 * Maximum allowed bid-ask spread.
	 */
	private double maximumSpread = 2.5;

	/**
	 * Strike interval. BANKNIFTY = 100 NIFTY = 50
	 */
	private int strikeStep = 100;

	/**
	 * Maximum strike distance from ATM. Used for broad filtering if required.
	 */
	private int maxStrikeDistance = 2000;

	/**
	 * Number of strikes to analyze on each side of ATM.
	 *
	 * Example: strikesAroundATM = 12
	 *
	 * 12 ITM +1 ATM 12 OTM
	 *
	 * = 25 CE + 25 PE = 50 contracts
	 */
	private int strikesAroundATM = 15;

	/**
	 * Contracts in one BANKNIFTY lot. Used for capital-based position sizing.
	 */
	private int lotSize = 30;

}