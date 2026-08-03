package com.banknifty.recommendation;

import com.banknifty.recommendation.model.DecisionContext;
import com.banknifty.recommendation.model.StrikeCandidate;
import com.banknifty.recommendation.model.TradeSetup;

/**
 * Builds a complete trade setup for an option candidate.
 *
 * Setup construction is deliberately separated from:
 *
 * 1. Underlying technical analysis 2. Market regime detection 3. Option-chain
 * candidate generation 4. Final recommendation decision
 *
 * The builder receives:
 *
 * - DecisionContext: underlying indicators, technical trend, market structure,
 * market regime, session context
 *
 * - StrikeCandidate: option-contract-specific information
 *
 * This allows the setup layer to determine whether a technically valid market
 * opportunity also has a suitable option contract.
 */
public interface TradeSetupBuilder {

	/**
	 * Build a trade setup for the supplied option candidate.
	 *
	 * The implementation may return a rejected / invalid setup when market
	 * conditions, setup quality or candidate quality are insufficient.
	 *
	 * @param context   complete underlying-market decision context
	 * @param candidate option candidate being evaluated
	 *
	 * @return evaluated trade setup
	 */
	TradeSetup build(DecisionContext context, StrikeCandidate candidate);
}