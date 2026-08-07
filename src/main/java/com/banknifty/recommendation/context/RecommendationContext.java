package com.banknifty.recommendation.context;

import java.math.BigDecimal;
import java.util.List;

import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.analysis.prediction.PredictionAnalysis;
import com.banknifty.analysis.reversal.ReversalAnalysis;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.context.MarketContext;
import com.banknifty.market.regime.MarketRegimeResult;
import com.banknifty.model.Candle;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.Signal;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.service.TrendAnalysisResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Context shared across the complete recommendation workflow.
 *
 * Extracted from the original DefaultRecommendationEngine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationContext {

	/*
	 * --------------------------------------------------------- Request
	 * ---------------------------------------------------------
	 */

	private RecommendationRequest request;

	/*
	 * --------------------------------------------------------- Market Data
	 * ---------------------------------------------------------
	 */

	private BigDecimal spot;

	private List<Candle> candles;

	private IndicatorSnapshot indicators;

	/*
	 * --------------------------------------------------------- Technical Analysis
	 * ---------------------------------------------------------
	 */

	private Signal signal;

	private TrendAnalysisResult structure;

	/*
	 * --------------------------------------------------------- Market Context
	 * ---------------------------------------------------------
	 */

	private MarketContext marketContext;

	private MarketRegimeResult regime;

	/*
	 * --------------------------------------------------------- Institutional
	 * Analysis ---------------------------------------------------------
	 */

	private InstitutionalAnalysis institutional;

	/*
	 * --------------------------------------------------------- Analysis Context
	 * ---------------------------------------------------------
	 */

	private AnalysisContext analysisContext;

	/*
	 * --------------------------------------------------------- Prediction
	 * ---------------------------------------------------------
	 */

	private PredictionAnalysis prediction;

	private ReversalAnalysis reversal;

	/*
	 * --------------------------------------------------------- Recommendation Gate
	 * ---------------------------------------------------------
	 */

	private int combinedConfidence;

	private boolean entryAllowed;

	private String gateReason;

	/*
	 * --------------------------------------------------------- Initial
	 * Recommendation ---------------------------------------------------------
	 */

	private TradeRecommendation winner;

}