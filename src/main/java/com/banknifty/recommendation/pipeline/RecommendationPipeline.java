package com.banknifty.recommendation.pipeline;

import java.util.List;

import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.RankedContract;
import com.banknifty.recommendation.model.RejectedContract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds every stage of option-chain analysis.
 *
 * Extracted from DefaultRecommendationEngine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationPipeline {

	/*
	 * --------------------------------------------------------- Entire analysed
	 * universe ---------------------------------------------------------
	 */

	private List<OptionAnalysis> all;

	/*
	 * --------------------------------------------------------- After trade-style
	 * filtering ---------------------------------------------------------
	 */

	private List<OptionAnalysis> eligible;

	/*
	 * --------------------------------------------------------- After validation
	 * ---------------------------------------------------------
	 */

	private List<OptionAnalysis> validatedEligible;

	/*
	 * --------------------------------------------------------- Final ranked
	 * contracts ---------------------------------------------------------
	 */

	private List<OptionAnalysis> ranked;

	/*
	 * --------------------------------------------------------- Best contract
	 * matching detected direction
	 * ---------------------------------------------------------
	 */

	private OptionAnalysis directionalBest;

	/*
	 * --------------------------------------------------------- Response objects
	 * ---------------------------------------------------------
	 */

	private List<RankedContract> top;

	private List<RejectedContract> rejected;

}