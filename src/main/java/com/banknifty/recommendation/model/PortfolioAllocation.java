package com.banknifty.recommendation.model;

import java.util.List;

public record PortfolioAllocation(List<AllocationLeg> legs, int cashPercent) {
}
