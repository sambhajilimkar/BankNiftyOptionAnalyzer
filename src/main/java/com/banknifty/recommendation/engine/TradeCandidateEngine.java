package com.banknifty.recommendation.engine;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.StrikeCandidate;

import java.util.List;

public interface TradeCandidateEngine {

	List<StrikeCandidate> buildCandidates(

			RecommendationRequest request,

			double spotPrice,

			List<OptionQuote> optionQuotes

	);

}