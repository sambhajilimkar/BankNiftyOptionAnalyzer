package com.banknifty.recommendation.engine;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.recommendation.model.RecommendationRequest;

import java.util.List;

public interface StrikeFilterEngine {

	List<OptionQuote> filter(

			RecommendationRequest request,

			double spotPrice,

			List<OptionQuote> options

	);

}