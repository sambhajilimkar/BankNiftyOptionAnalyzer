package com.banknifty.options.service;

import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.broker.model.OptionQuote;

import java.util.List;

public interface OptionUniverseService {

	List<OptionQuote> loadUniverse(RecommendationRequest request);

}