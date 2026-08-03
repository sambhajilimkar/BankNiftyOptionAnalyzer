package com.banknifty.market.state;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MarketStateAnalysis {

	private MarketState state;

	private int confidence;

	private boolean tradable;

	private boolean reversalLikely;

	private double continuationProbability;

	private double reversalProbability;

	private List<String> reasons;

}