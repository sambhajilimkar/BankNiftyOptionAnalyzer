package com.banknifty.recommendation.engine;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

public interface OptionChainAnalyticsEngine {

	/**
	 * Performs complete institutional option chain analysis.
	 *
	 * Current Snapshot + Previous Snapshot
	 *
	 * -> Institutional Analysis
	 */
	InstitutionalAnalysis analyze(OptionSnapshot previousSnapshot, OptionSnapshot currentSnapshot);

}