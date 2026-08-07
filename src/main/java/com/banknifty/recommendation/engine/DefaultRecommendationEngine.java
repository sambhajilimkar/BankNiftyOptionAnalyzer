package com.banknifty.recommendation.engine;

import org.springframework.stereotype.Service;

import com.banknifty.recommendation.context.RecommendationContext;
import com.banknifty.recommendation.context.RecommendationContextFactory;
import com.banknifty.recommendation.decision.RecommendationDecisionService;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.RecommendationResponse;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.pipeline.RecommendationPipeline;
import com.banknifty.recommendation.pipeline.RecommendationPipelineService;
import com.banknifty.recommendation.response.RecommendationResponseBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the complete recommendation workflow.
 *
 * Business logic has been extracted into dedicated services:
 *
 * - RecommendationContextFactory - RecommendationPipelineService -
 * RecommendationDecisionService - RecommendationResponseBuilder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRecommendationEngine implements RecommendationEngine {

	private final RecommendationContextFactory contextFactory;

	private final RecommendationPipelineService pipelineService;

	private final RecommendationDecisionService decisionService;

	private final RecommendationResponseBuilder responseBuilder;

	@Override
	public RecommendationResponse recommend(RecommendationRequest request) {

		/*
		 * --------------------------------------------------------- Build Context
		 * ---------------------------------------------------------
		 */
		RecommendationContext context = contextFactory.build(request);

		/*
		 * --------------------------------------------------------- Initial WAIT
		 * Recommendation ---------------------------------------------------------
		 */
		TradeRecommendation winner = decisionService.noTrade(

				context.getRequest(),

				context.getSpot(),

				context.getSignal(),

				context.getInstitutional(),

				context.getGateReason(),

				context.getCombinedConfidence());

		/*
		 * --------------------------------------------------------- Analyse Complete
		 * Option Chain ---------------------------------------------------------
		 */
		RecommendationPipeline pipeline = pipelineService.analyse(context);

		/*
		 * --------------------------------------------------------- Build Final
		 * Response ---------------------------------------------------------
		 */
		return responseBuilder.build(

				context,

				pipeline,

				winner);
	}
}