package com.banknifty.recommendation.model;

import lombok.Builder;

@Builder
public record MarketHealth(

        double trend,

        double momentum,

        double liquidity,

        double volatility,

        double openInterest,

        double overall

) {
}