package com.banknifty.model;

import com.banknifty.enums.TimeFrame;
import lombok.Builder;

@Builder
public record TimeFrameAnalysis(

        TimeFrame timeFrame,

        TrendDirection trend,

        int score,

        boolean bullish,

        boolean bearish,

        boolean confirmed

) {
}