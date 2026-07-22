package com.banknifty.optionchain.service;

import java.math.BigDecimal;

import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.recommendation.model.RecommendationRequest;

public interface OptionSnapshotService {

	/**
	 * Returns latest complete option chain snapshot.
	 */
	OptionSnapshot getLatestSnapshot();

	/**
	 * Builds a snapshot for the requested expiry using the supplied live underlying
	 * price. Supplying the price avoids deriving ATM from an option premium.
	 */
	OptionSnapshot getLatestSnapshot(RecommendationRequest request, BigDecimal spotPrice);

}
