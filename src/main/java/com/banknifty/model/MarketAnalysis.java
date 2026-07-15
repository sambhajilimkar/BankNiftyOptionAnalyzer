package com.banknifty.model;

import com.banknifty.enums.MarketRegime;
import lombok.Builder;

@Builder
public record MarketAnalysis(

        MarketRegime regime,

        TrendDirection trendDirection,

        int confidence,

        boolean tradable,

        String reason

) {
}