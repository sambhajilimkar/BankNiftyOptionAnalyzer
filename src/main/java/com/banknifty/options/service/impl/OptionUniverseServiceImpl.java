package com.banknifty.options.service.impl;

import com.banknifty.broker.BrokerProvider;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.recommendation.model.RecommendationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionUniverseServiceImpl implements OptionUniverseService {

	private final BrokerProvider brokerProvider;

	@Override
	public List<OptionQuote> loadUniverse(RecommendationRequest request) {

		/*
		 * Broker decides how to fetch:
		 *
		 * Zerodha Angel Upstox
		 */

		return brokerProvider.optionChain(

				request.instrument(),

				request.expiryType().name()

		);

	}

}