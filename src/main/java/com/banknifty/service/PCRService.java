package com.banknifty.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Put-Call Ratio (PCR) calculation service.
 */
@Service
public class PCRService {

    /**
     * Calculates PCR using total Put OI and Call OI.
     *
     * @param putOpenInterest total put open interest
     * @param callOpenInterest total call open interest
     * @return PCR value rounded to 2 decimals
     */
    public BigDecimal calculate(long putOpenInterest, long callOpenInterest) {

        if (callOpenInterest <= 0) {
            throw new IllegalArgumentException("Call Open Interest must be greater than zero.");
        }

        return BigDecimal.valueOf(putOpenInterest)
                .divide(BigDecimal.valueOf(callOpenInterest), 2, RoundingMode.HALF_UP);
    }

    /**
     * PCR > 1 indicates bullish sentiment.
     */
    public boolean isBullish(BigDecimal pcr) {
        return pcr.compareTo(BigDecimal.ONE) > 0;
    }

    /**
     * PCR < 1 indicates bearish sentiment.
     */
    public boolean isBearish(BigDecimal pcr) {
        return pcr.compareTo(BigDecimal.ONE) < 0;
    }

    /**
     * Neutral market.
     */
    public boolean isNeutral(BigDecimal pcr) {
        return pcr.compareTo(BigDecimal.ONE) == 0;
    }
}
