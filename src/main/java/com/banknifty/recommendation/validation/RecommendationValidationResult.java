package com.banknifty.recommendation.validation;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class RecommendationValidationResult {

    private boolean valid;

    @Builder.Default
    private List<String> rejectedReasons = new ArrayList<>();

    public boolean isValid() {
        return valid;
    }

    public List<String> getRejectedReasons() {
        return rejectedReasons;
    }

    public void reject(String reason) {

        valid = false;
        rejectedReasons.add(reason);
    }

    public void approve() {

        valid = true;
    }

    public boolean hasErrors() {

        return !rejectedReasons.isEmpty();
    }

}