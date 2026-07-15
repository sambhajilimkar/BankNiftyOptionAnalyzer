package com.banknifty.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation.candidate")
public class TradeCandidateProperties {

	/**
	 * Minimum traded volume.
	 */
	private long minimumVolume = 50000;

	/**
	 * Minimum Open Interest.
	 */
	private long minimumOpenInterest = 200000;

	/**
	 * Minimum premium.
	 */
	private double minimumPremium = 100;

	/**
	 * Maximum premium.
	 */
	private double maximumPremium = 500;

	/**
	 * Maximum bid ask spread (%).
	 */
	private double maximumSpread = 0.50;

}