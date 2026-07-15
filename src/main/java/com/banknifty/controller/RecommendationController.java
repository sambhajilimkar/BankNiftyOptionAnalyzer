package com.banknifty.controller;

import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.recommendation.engine.RecommendationEngine;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendationController {

	private final RecommendationEngine recommendationEngine;

	@GetMapping
	public ResponseEntity<TradeRecommendation> recommend(@RequestParam(defaultValue = "NIFTY BANK") String instrument,
			@RequestParam(defaultValue = "MONTHLY") ExpiryType expiryType,
			@RequestParam(defaultValue = "INTRADAY") TradingStyle tradingStyle,
			@RequestParam(defaultValue = "BALANCED") RiskProfile riskProfile,
			@RequestParam(required = false) Double capital) {

		RecommendationRequest request = RecommendationRequest.builder().instrument(instrument).expiryType(expiryType)
				.tradingStyle(tradingStyle).riskProfile(riskProfile).capital(capital).build();

		return ResponseEntity.ok(recommendationEngine.recommend(request));
	}
}
