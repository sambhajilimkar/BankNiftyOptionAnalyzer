package com.banknifty.controller;

import com.banknifty.enums.ExpiryType;
import com.banknifty.model.Recommendation;
import com.banknifty.service.OptionRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendationController {

	private final OptionRecommendationService optionRecommendationService;

	@GetMapping
	public ResponseEntity<Recommendation> recommend(

			@RequestParam(defaultValue = "BANKNIFTY") String instrument,

			@RequestParam(defaultValue = "MONTHLY") ExpiryType expiryType) {

		Recommendation recommendation = optionRecommendationService.recommend(instrument, expiryType);

		return ResponseEntity.ok(recommendation);

	}

}