package com.banknifty.service;

import com.banknifty.model.OptionRecommendation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Calculates Max Pain strike from option recommendations.
 *
 * NOTE:
 * This is the initial implementation. It can later be enhanced to use
 * full option-chain CE/PE Open Interest data.
 */
@Service
public class MaxPainService {

    /**
     * Returns the strike with the highest confidence as the current
     * Max Pain approximation.
     */
    public OptionRecommendation calculate(List<OptionRecommendation> recommendations) {

        if (recommendations == null || recommendations.isEmpty()) {
            throw new IllegalArgumentException("Option recommendations cannot be empty.");
        }

        return recommendations.stream()
                .max(Comparator.comparingInt(OptionRecommendation::confidence))
                .orElseThrow();
    }

    /**
     * Indicates whether a valid Max Pain recommendation exists.
     */
    public boolean hasRecommendation(List<OptionRecommendation> recommendations) {
        return recommendations != null && !recommendations.isEmpty();
    }
}
