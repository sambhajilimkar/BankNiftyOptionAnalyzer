package com.banknifty.recommendation.engine;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.InstitutionalAnalysis;

public interface InstitutionalAnalysisEngine {

	/**
	 * Performs complete institutional analysis on the option chain.
	 *
	 * Input : current and previous complete option-chain snapshots. The previous
	 * snapshot is optional; when it is unavailable, OI build-up is omitted.
	 */
	InstitutionalAnalysis analyze(OptionSnapshot currentSnapshot, OptionSnapshot previousSnapshot,
			AnalysisContext context);

}
