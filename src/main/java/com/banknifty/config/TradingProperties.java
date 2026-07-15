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
    private double maximumPremium = 400;

    /**
     * Minimum volume.
     */
    private long minimumVolume = 1000;

    /**
     * Minimum Open Interest.
     */
    private long minimumOpenInterest = 5000;

    /**
     * Maximum bid ask spread.
     */
    private double maximumSpread = 2.5;

    /**
     * Strike interval.
     * BankNifty = 100
     */
    private int strikeStep = 100;

    /**
     * Maximum distance from ATM.
     */
    private int maxStrikeDistance = 200;

}