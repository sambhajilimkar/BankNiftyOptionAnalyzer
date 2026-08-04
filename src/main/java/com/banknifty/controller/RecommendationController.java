package com.banknifty.controller;

import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.recommendation.engine.RecommendationEngine;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.model.RecommendationResponseV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

	private final RecommendationEngine recommendationEngine;

	@GetMapping
	public ResponseEntity<TradeRecommendation> recommend(

			@RequestParam(defaultValue = "BANKNIFTY") String instrument,

			@RequestParam(defaultValue = "MONTHLY") ExpiryType expiryType,

			@RequestParam(defaultValue = "SWING") TradingStyle tradingStyle,

			@RequestParam(defaultValue = "BALANCED") RiskProfile riskProfile,

			@RequestParam(required = false) Double capital) {

		RecommendationRequest request = RecommendationRequest.builder().instrument(instrument).expiryType(expiryType)
				.tradingStyle(tradingStyle).riskProfile(riskProfile).capital(capital).build();

		log.info("Recommendation request is using DefaultRecommendationEngine: instrument={}, expiryType={}, "
				+ "tradingStyle={}, riskProfile={}", instrument, expiryType, tradingStyle, riskProfile);

		TradeRecommendation recommendation = recommendationEngine.recommend(request);

		return ResponseEntity.ok(recommendation);

	}

	@GetMapping("/v2")
	public ResponseEntity<RecommendationResponseV2> recommendV2(
			@RequestParam(defaultValue = "BANKNIFTY") String instrument,
			@RequestParam(defaultValue = "MONTHLY") ExpiryType expiryType,
			@RequestParam(defaultValue = "SWING") TradingStyle tradingStyle,
			@RequestParam(defaultValue = "BALANCED") RiskProfile riskProfile,
			@RequestParam(required = false) Double capital) {
		RecommendationRequest request = RecommendationRequest.builder().instrument(instrument).expiryType(expiryType)
				.tradingStyle(tradingStyle).riskProfile(riskProfile).capital(capital).build();
		return ResponseEntity.ok(recommendationEngine.recommendV2(request));
	}

}
