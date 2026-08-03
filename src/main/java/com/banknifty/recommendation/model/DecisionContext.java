package com.banknifty.recommendation.model;

import java.util.List;

import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;
import com.banknifty.market.context.MarketContext;
import com.banknifty.market.regime.MarketRegimeResult;
import com.banknifty.service.TrendAnalysisResult;
import lombok.Builder;

/**
 * Aggregates all underlying-market information required by the recommendation
 * and trade-setup decision layers.
 *
 * This context intentionally contains market-level information only.
 * Option-contract-specific information such as StrikeCandidate is supplied
 * separately to the TradeSetupBuilder.
 */
@Builder
public record DecisionContext(

		/*
		 * Original recommendation request.
		 */
		RecommendationRequest request,

		/*
		 * Detailed technical indicator snapshot.
		 */
		IndicatorSnapshot indicators,

		List<Candle> candles,

		/*
		 * Session / expiry / event / global market context.
		 */
		MarketContext marketContext,

		/*
		 * Detected market regime.
		 */
		MarketRegimeResult regime,

		/*
		 * Existing underlying BANKNIFTY trend analysis.
		 *
		 * Contains:
		 *
		 * - technical score - technical confidence - support / resistance - breakout /
		 * breakdown state - pivot / CPR - open-interest structure
		 *
		 * This allows the setup layer to reuse calculations already performed by
		 * TrendAnalysisService instead of calculating market structure again.
		 */
		TrendAnalysisResult trendAnalysis

) {

	/**
	 * Indicates whether the market-context layer currently permits creation of a
	 * new trade.
	 */
	public boolean tradeAllowed() {

		return marketContext == null || marketContext.tradeAllowed();
	}

	/**
	 * Returns the directional technical score calculated by TrendAnalysisService.
	 */
	public int technicalScore() {

		return trendAnalysis != null && trendAnalysis.technicalScore() != null ? trendAnalysis.technicalScore() : 0;
	}

	/**
	 * Returns the technical-analysis confidence.
	 */
	public int technicalConfidence() {

		return trendAnalysis != null && trendAnalysis.confidence() != null ? trendAnalysis.confidence() : 0;
	}

	/**
	 * Convenience method for setup detection.
	 */
	public boolean breakout() {

		return trendAnalysis != null && trendAnalysis.breakout();
	}

	/**
	 * Convenience method for setup detection.
	 */
	public boolean breakdown() {

		return trendAnalysis != null && trendAnalysis.breakdown();
	}

	/**
	 * Convenience method for setup detection.
	 */
	public boolean nearSupport() {

		return trendAnalysis != null && trendAnalysis.nearSupport();
	}

	/**
	 * Convenience method for setup detection.
	 */
	public boolean nearResistance() {

		return trendAnalysis != null && trendAnalysis.nearResistance();
	}
}
