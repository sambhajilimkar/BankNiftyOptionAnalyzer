package com.banknifty.model;

import com.banknifty.enums.CandlestickPattern;
import lombok.Builder;

@Builder
public record PatternResult(

        CandlestickPattern pattern,

        boolean bullish,

        boolean bearish,

        int score,

        String description

) {
}