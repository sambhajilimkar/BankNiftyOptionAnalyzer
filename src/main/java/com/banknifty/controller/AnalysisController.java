package com.banknifty.controller;

import com.banknifty.model.OptionRecommendation;
import com.banknifty.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisService analysisService;

	/**
	 * Analyze any instrument.
	 *
	 * Example:
	 *
	 * /api/v1/analysis?instrumentToken=12345 &tradingSymbol=BANKNIFTY25JUL59000CE
	 * &exchange=NFO &interval=5minute
	 */
	@GetMapping
	public OptionRecommendation analyze(

			@RequestParam Long instrumentToken,

			@RequestParam String tradingSymbol,

			@RequestParam(defaultValue = "NFO") String exchange,

			@RequestParam(defaultValue = "5minute") String interval,

			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

		return analysisService.analyze(

				instrumentToken,

				tradingSymbol,

				exchange,

				interval,

				from,

				to

		);

	}

	/**
	 * Convenience endpoint for BANKNIFTY.
	 */
	@GetMapping("/banknifty")
	public OptionRecommendation bankNifty() {

		return analysisService.analyze(

				260105L, // <-- Replace with configurable token later

				"BANKNIFTY",

				"NFO",

				"5minute",

				LocalDateTime.now().minusDays(5),

				LocalDateTime.now()

		);

	}

}