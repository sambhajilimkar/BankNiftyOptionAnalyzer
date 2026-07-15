package com.banknifty.service;

import com.banknifty.enums.ExpiryType;
import com.banknifty.model.Recommendation;

public interface OptionRecommendationService {

	Recommendation recommend(

			String underlying,

			ExpiryType expiryType

	);

}