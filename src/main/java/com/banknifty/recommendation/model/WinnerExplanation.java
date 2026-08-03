package com.banknifty.recommendation.model;

import java.util.List;

/** Human-readable explanation of why a contract was promoted to executable winner. */
public record WinnerExplanation(String grade, double finalScore, List<String> highlights) {
}
