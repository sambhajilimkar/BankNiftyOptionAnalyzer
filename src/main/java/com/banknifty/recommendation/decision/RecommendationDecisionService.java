package com.banknifty.recommendation.decision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.MarketBias;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskLevel;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.Signal;
import com.banknifty.recommendation.model.TradeRecommendation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationDecisionService {

	private final TradingProperties tradingProperties;

	public TradeRecommendation trade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, OptionAnalysis best) {

		List<String> reasons = new ArrayList<>(signal.getReasons());

		reasons.addAll(best.getReasons());

		reasons.add("Full option-chain institutional analysis confirmed the selected contract");

		int confidence = (int) Math
				.round(Math.min(100, (signal.getConfidence() * 0.55) + (institutional.getConfidence() * 0.45)));

		TradeLevels levels = tradeLevels(best.getEntry(), request.tradingStyle(), request.riskProfile());

		return TradeRecommendation.builder().action(RecommendationAction.BUY).instrument(request.instrument())
				.expiryDate(best.getCandidate().getExpiry()).expiryLabel(request.expiryType().name())
				.optionType(best.getCandidate().getOptionType()).strikePrice(best.getCandidate().getStrike())
				.spotPrice(spotPrice).optionPrice(best.getCandidate().getPremium()).entryMin(best.getEntry())
				.entryMax(best.getEntry()).stopLoss(levels.stopLoss()).target1(levels.target1())
				.target2(levels.target2()).target3(levels.target3()).confidence(confidence)
				.risk(riskLevel(request.riskProfile()))
				.quantity(positionLots(request.capital(), best.getEntry(), request.riskProfile()))
				.holdingTime(holdingTime(request.tradingStyle())).reasons(List.copyOf(reasons))
				.rejectedReasons(List.of()).institutionalAnalysis(institutional)
				.technicalConfidence(signal.getConfidence())
				.technicalBias(marketBias(signal.getOptionType(), signal.getConfidence())).build();
	}

	public TradeRecommendation noTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, String rejectedReason) {

		return noTrade(request, spotPrice, signal, institutional, rejectedReason, signal.getConfidence());
	}

	public TradeRecommendation noTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, String rejectedReason, int recommendationConfidence) {

		return TradeRecommendation.builder().action(RecommendationAction.WAIT).instrument(request.instrument())
				.expiryLabel(request.expiryType().name()).spotPrice(spotPrice).optionType(null)
				.confidence(recommendationConfidence).risk(riskLevel(request.riskProfile())).quantity(0)
				.holdingTime("No trade").reasons(List.copyOf(signal.getReasons())).rejectedReasons(List.of(rejectedReason))
				.institutionalAnalysis(institutional).technicalConfidence(signal.getConfidence())
				.technicalBias(marketBias(signal.getOptionType(), signal.getConfidence())).build();
	}

	/*
	 * --------------------------------------------------------- Helper methods
	 * (Copied exactly from DefaultRecommendationEngine)
	 * ---------------------------------------------------------
	 */

	private TradeLevels tradeLevels(BigDecimal entry, TradingStyle style, RiskProfile profile) {

		double[] styleMultipliers = switch (style) {

		case SCALPING -> new double[] { 0.95, 1.08, 1.15, 1.22 };

		case INTRADAY -> new double[] { 0.90, 1.12, 1.25, 1.40 };

		case SWING -> new double[] { 0.85, 1.20, 1.40, 1.60 };

		case POSITIONAL -> new double[] { 0.80, 1.30, 1.60, 2.00 };
		};

		double targetAdjustment = switch (profile) {

		case CONSERVATIVE -> -0.03;

		case MODERATE -> -0.01;

		case BALANCED -> 0;

		case AGGRESSIVE -> 0.05;
		};

		double stopAdjustment = switch (profile) {

		case CONSERVATIVE -> 0.03;

		case MODERATE -> 0.01;

		case BALANCED -> 0;

		case AGGRESSIVE -> -0.03;
		};

		return new TradeLevels(

				level(entry, styleMultipliers[0] + stopAdjustment),

				level(entry, styleMultipliers[1] + targetAdjustment),

				level(entry, styleMultipliers[2] + targetAdjustment),

				level(entry, styleMultipliers[3] + targetAdjustment));
	}

	private int positionLots(Double capital, BigDecimal entry, RiskProfile profile) {

		if (capital == null || capital <= 0 || entry == null || entry.signum() <= 0) {

			return 1;
		}

		double costPerLot = entry.doubleValue() * Math.max(1, tradingProperties.getLotSize());

		int affordableLots = (int) Math.floor(capital / costPerLot);

		return Math.max(1, Math.min(maxLots(profile), affordableLots));
	}

	private int maxLots(RiskProfile profile) {

		return switch (profile) {

		case CONSERVATIVE -> 1;

		case MODERATE -> 2;

		case BALANCED -> 3;

		case AGGRESSIVE -> 5;
		};
	}

	private String holdingTime(TradingStyle style) {

		return switch (style) {

		case SCALPING -> "5-15 minutes";

		case INTRADAY -> "30-90 minutes";

		case SWING -> "1-3 trading days";

		case POSITIONAL -> "1-4 weeks";
		};
	}

	private RiskLevel riskLevel(RiskProfile profile) {

		return switch (profile) {

		case CONSERVATIVE -> RiskLevel.LOW;

		case MODERATE, BALANCED -> RiskLevel.MEDIUM;

		case AGGRESSIVE -> RiskLevel.HIGH;
		};
	}

	private MarketBias marketBias(OptionType type, int confidence) {

		if (type == OptionType.CE) {

			return confidence >= 80 ? MarketBias.STRONG_BULLISH : MarketBias.BULLISH;
		}

		return confidence >= 80 ? MarketBias.STRONG_BEARISH : MarketBias.BEARISH;
	}

	private BigDecimal level(BigDecimal value, double multiplier) {

		return value.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
	}

	private record TradeLevels(BigDecimal stopLoss, BigDecimal target1, BigDecimal target2, BigDecimal target3) {
	}
}