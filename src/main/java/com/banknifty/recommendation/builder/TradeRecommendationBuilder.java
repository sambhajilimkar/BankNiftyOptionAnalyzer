package com.banknifty.recommendation.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banknifty.analysis.MarketBias;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskLevel;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.banknifty.recommendation.service.TechnicalAnalysisService.Signal;

@Component
public class TradeRecommendationBuilder {

	public TradeRecommendation buildTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, OptionAnalysis analysis, int executableConfidence) {

		List<String> reasons = new ArrayList<>(signal.reasons());

		if (analysis.getReasons() != null) {
			reasons.addAll(analysis.getReasons());
		}

		reasons.add("Institutional confirmation");

		BigDecimal entry = analysis.getEntry();

		BigDecimal stopLoss = calculateStopLoss(entry, request.tradingStyle());

		BigDecimal target1 = calculateTarget1(entry, request.tradingStyle());

		BigDecimal target2 = calculateTarget2(entry, request.tradingStyle());

		BigDecimal target3 = calculateTarget3(entry, request.tradingStyle());

		return TradeRecommendation.builder()

				.action(RecommendationAction.BUY)

				.instrument(request.instrument())

				.expiryDate(analysis.getCandidate().getExpiry())

				.expiryLabel(request.expiryType().name())

				.optionType(analysis.getCandidate().getOptionType())

				.strikePrice(analysis.getCandidate().getStrike())

				.spotPrice(spotPrice)

				.optionPrice(analysis.getCandidate().getPremium())

				.entryMin(entry)

				.entryMax(entry)

				.stopLoss(stopLoss)

				.target1(target1)

				.target2(target2)

				.target3(target3)

				.confidence(executableConfidence)

				.risk(riskLevel(request.riskProfile()))

				.quantity(positionLots(request.capital(), entry))

				.holdingTime(holdingTime(request.tradingStyle()))

				.reasons(List.copyOf(reasons))

				.rejectedReasons(List.of())

				.institutionalAnalysis(institutional)

				.technicalConfidence(signal.confidence())

				.technicalBias(marketBias(signal.optionType()))

				.build();
	}

	public TradeRecommendation buildNoTrade(RecommendationRequest request, BigDecimal spotPrice, Signal signal,
			InstitutionalAnalysis institutional, String rejectedReason, int recommendationConfidence) {

		return TradeRecommendation.builder()

				.action(RecommendationAction.WAIT)

				.instrument(request.instrument())

				.expiryLabel(request.expiryType().name())

				.spotPrice(spotPrice)

				.confidence(recommendationConfidence)

				.risk(riskLevel(request.riskProfile()))

				.quantity(0)

				.holdingTime("No Trade")

				.reasons(List.copyOf(signal.reasons()))

				.rejectedReasons(List.of(rejectedReason))

				.institutionalAnalysis(institutional)

				.technicalConfidence(signal.confidence())

				.technicalBias(marketBias(signal.optionType()))

				.build();
	}

	private BigDecimal calculateStopLoss(BigDecimal entry, TradingStyle style) {

		return entry.subtract(entry.multiply(stopLossPercent(style)));
	}

	private BigDecimal calculateTarget1(BigDecimal entry, TradingStyle style) {

		return entry.add(entry.multiply(target1Percent(style)));
	}

	private BigDecimal calculateTarget2(BigDecimal entry, TradingStyle style) {

		return entry.add(entry.multiply(target2Percent(style)));
	}

	private BigDecimal calculateTarget3(BigDecimal entry, TradingStyle style) {

		return entry.add(entry.multiply(target3Percent(style)));
	}

	private BigDecimal stopLossPercent(TradingStyle style) {

		return switch (style) {

		case SCALPING -> percent(5);

		case INTRADAY -> percent(8);

		case SWING -> percent(15);

		case POSITIONAL -> percent(20);

		default -> percent(10);
		};
	}

	private BigDecimal target1Percent(TradingStyle style) {

		return switch (style) {

		case SCALPING -> percent(8);

		case INTRADAY -> percent(12);

		case SWING -> percent(20);

		case POSITIONAL -> percent(30);

		default -> percent(15);
		};
	}

	private BigDecimal target2Percent(TradingStyle style) {

		return switch (style) {

		case SCALPING -> percent(12);

		case INTRADAY -> percent(18);

		case SWING -> percent(35);

		case POSITIONAL -> percent(50);

		default -> percent(25);
		};
	}

	private BigDecimal target3Percent(TradingStyle style) {

		return switch (style) {

		case SCALPING -> percent(16);

		case INTRADAY -> percent(25);

		case SWING -> percent(50);

		case POSITIONAL -> percent(80);

		default -> percent(35);
		};
	}

	private Integer positionLots(Double capital, BigDecimal premium) {

		if (capital == null || premium == null || premium.compareTo(BigDecimal.ZERO) <= 0) {
			return 1;
		}

		int quantity = (int) (capital / premium.doubleValue());

		return Math.max(1, quantity);
	}

	private String holdingTime(TradingStyle tradingStyle) {

		return switch (tradingStyle) {

		case SCALPING -> "5-15 Minutes";

		case INTRADAY -> "Same Day";

		case SWING -> "2-5 Days";

		case POSITIONAL -> "1-4 Weeks";

		default -> "Unknown";
		};
	}

	private RiskLevel riskLevel(RiskProfile profile) {

		return switch (profile) {

		case CONSERVATIVE -> RiskLevel.LOW;

		case MODERATE -> RiskLevel.MEDIUM;

		case BALANCED -> RiskLevel.MEDIUM;

		case AGGRESSIVE -> RiskLevel.HIGH;

		default -> RiskLevel.MEDIUM;
		};
	}

	private MarketBias marketBias(OptionType optionType) {

		return optionType == OptionType.CE ? MarketBias.BULLISH : MarketBias.BEARISH;
	}

	private BigDecimal percent(double value) {

		return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
	}
}