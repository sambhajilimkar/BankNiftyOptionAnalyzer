package com.banknifty.recommendation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.banknifty.entity.RecommendationAuditEntity;
import com.banknifty.recommendation.model.RecommendationResponseV2;
import com.banknifty.repository.RecommendationAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Persists every V2 decision before later 5/15/30/60-minute outcome evaluation. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationAuditService {
	private final RecommendationAuditRepository repository;
	private final ObjectMapper objectMapper;

	public void record(RecommendationResponseV2 response) {
		try {
			String symbol = response.winner() == null ? null : response.winner().optionType() == null ? null
					: response.winner().strikePrice() + " " + response.winner().optionType();
			double score = response.winnerExplanation() == null ? 0 : response.winnerExplanation().finalScore();
			LocalDateTime generated = response.generatedAt();
			repository.save(RecommendationAuditEntity.builder().generatedAt(generated)
					.action(response.winner().action().name()).instrument(response.winner().instrument()).winnerSymbol(symbol)
					.winnerScore(score).evaluationStatus("PENDING") .evaluateAt5m(generated.plusMinutes(5))
					.evaluateAt15m(generated.plusMinutes(15)).evaluateAt30m(generated.plusMinutes(30))
					.evaluateAt60m(generated.plusMinutes(60)).payloadJson(objectMapper.writeValueAsString(response)).build());
		} catch (JsonProcessingException | RuntimeException exception) {
			// Audit outage must not suppress a live recommendation.
			log.warn("Unable to persist recommendation audit record", exception);
		}
	}
}
