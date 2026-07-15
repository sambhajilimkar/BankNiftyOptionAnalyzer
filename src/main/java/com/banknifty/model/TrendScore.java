package com.banknifty.model;

import lombok.Builder;

import java.util.List;

@Builder
public record TrendScore(

        int totalScore,

        int confidence,

        TrendDirection trendDirection,

        String marketRegime,

        boolean buyCE,

        boolean buyPE,

        List<String> reasons

) {
}