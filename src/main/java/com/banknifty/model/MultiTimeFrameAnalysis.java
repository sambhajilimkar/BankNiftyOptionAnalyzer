package com.banknifty.model;

import lombok.Builder;

import java.util.List;

@Builder
public record MultiTimeFrameAnalysis(

        List<TimeFrameAnalysis> analyses,

        int overallScore,

        boolean bullish,

        boolean bearish,

        boolean confirmed

) {
}