package com.banknifty.controller;

import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.recommendation.engine.RecommendationEngine;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

	private final RecommendationEngine recommendationEngine;

	@GetMapping
	public ResponseEntity<RecommendationResponse> recommend(
			@RequestParam(defaultValue = "BANKNIFTY") String instrument,
			@RequestParam(defaultValue = "MONTHLY") ExpiryType expiryType,
			@RequestParam(defaultValue = "SWING") TradingStyle tradingStyle,
			@RequestParam(defaultValue = "BALANCED") RiskProfile riskProfile,
			@RequestParam(required = false) Double capital) {

		RecommendationRequest request = buildRequest(instrument, expiryType, tradingStyle, riskProfile, capital);

		log.info("Recommendation Request : instrument={}, expiryType={}, tradingStyle={}, riskProfile={}, capital={}",
				instrument, expiryType, tradingStyle, riskProfile, capital);

		return ResponseEntity.ok(recommendationEngine.recommend(request));
	}

	private RecommendationRequest buildRequest(String instrument, ExpiryType expiryType, TradingStyle tradingStyle,
			RiskProfile riskProfile, Double capital) {

		return RecommendationRequest.builder().instrument(instrument).expiryType(expiryType).tradingStyle(tradingStyle)
				.riskProfile(riskProfile).capital(capital).build();
	}
}