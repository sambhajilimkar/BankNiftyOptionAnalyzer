package com.banknifty.recommendation.engine;

import com.banknifty.analysis.IndicatorPipeline;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskLevel;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.model.Candle;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.recommendation.model.TradeRecommendation;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultRecommendationEngine implements RecommendationEngine {

	private static final String NSE = "NSE";
	private static final String INTERVAL = "5minute";
	private static final int HISTORY_DAYS = 10;
	private static final int MINIMUM_CONFIDENCE = 70;

	private final IndicatorPipeline indicatorPipeline;
	private final KiteHistoricalDataProvider historicalDataProvider;
	private final KiteInstrumentProvider instrumentProvider;
	private final KiteQuoteProvider quoteProvider;
	private final OptionUniverseService optionUniverseService;
	private final TradingProperties tradingProperties;

	@Override
	public TradeRecommendation recommend(RecommendationRequest request) {
		RecommendationRequest normalized = normalize(request);
		String spotSymbol = spotSymbol(normalized.instrument());
		BigDecimal spotPrice = liveSpotPrice(spotSymbol);
		IndicatorSnapshot indicators = indicatorPipeline.calculate(historicalCandles(spotSymbol));
		Signal signal = signal(indicators);

		if (signal.confidence() < MINIMUM_CONFIDENCE) {
			return noTrade(normalized, spotPrice, signal.reasons(),
					"Technical confirmation is below " + MINIMUM_CONFIDENCE + "%");
		}

		List<OptionQuote> universe = optionUniverseService.loadUniverse(normalized);
		OptionQuote contract = universe.stream().filter(quote -> quote.optionType() == signal.optionType())
				.filter(quote -> isTradable(quote, spotPrice, normalized.riskProfile()))
				.max(Comparator.comparingInt(
						quote -> contractScore(quote, signal.optionType(), spotPrice, normalized.riskProfile())))
				.orElse(null);

		if (contract == null) {
			return noTrade(normalized, spotPrice, signal.reasons(),
					"No live option contract matched the premium and strike-risk rules");
		}

		BigDecimal entry = contract.ltp();
		Targets targets = targets(entry, normalized.tradingStyle());
		List<String> reasons = new ArrayList<>(signal.reasons());
		reasons.add("Selected live " + contract.optionType() + " contract " + contract.tradingSymbol());
		reasons.add("Strike placement and premium satisfy " + normalized.riskProfile() + " rules");
		reasons.add("News/global-market risk filters are not enabled yet");

		return TradeRecommendation.builder().action(RecommendationAction.BUY).instrument(normalized.instrument())
				.expiryDate(contract.expiry()).expiryLabel(normalized.expiryType().name())
				.optionType(contract.optionType()).strikePrice(contract.strike()).spotPrice(spotPrice)
				.optionPrice(entry).entryMin(targets.entryMin()).entryMax(targets.entryMax())
				.stopLoss(targets.stopLoss()).target1(targets.target1()).target2(targets.target2())
				.target3(targets.target3()).confidence(signal.confidence()).risk(riskLevel(normalized.riskProfile()))
				.quantity(positionLots(normalized.capital(), entry)).holdingTime(holdingTime(normalized.tradingStyle()))
				.reasons(List.copyOf(reasons))
				.rejectedReasons(List.of("OI, Greeks, global market, and news filters are pending live data feeds"))
				.build();
	}

	private Signal signal(IndicatorSnapshot indicators) {
		int bullish = 0;
		int bearish = 0;
		List<String> bullishReasons = new ArrayList<>();
		List<String> bearishReasons = new ArrayList<>();

		if (indicators.ema().bullishAlignment()) {
			bullish += 25;
			bullishReasons.add("EMA bullish alignment");
		}
		if (indicators.ema().bearishAlignment()) {
			bearish += 25;
			bearishReasons.add("EMA bearish alignment");
		}
		if (indicators.rsi().bullish() && indicators.rsi().rising()) {
			bullish += 15;
			bullishReasons.add("RSI rising");
		}
		if (indicators.rsi().bearish() && indicators.rsi().falling()) {
			bearish += 15;
			bearishReasons.add("RSI falling");
		}
		if (indicators.macd().bullish() || indicators.macd().bullishCross()) {
			bullish += 20;
			bullishReasons.add("MACD bullish");
		}
		if (indicators.macd().bearish() || indicators.macd().bearishCross()) {
			bearish += 20;
			bearishReasons.add("MACD bearish");
		}
		if (indicators.vwap().aboveVWAP()) {
			bullish += 15;
			bullishReasons.add("Price above VWAP");
		} else {
			bearish += 15;
			bearishReasons.add("Price below VWAP");
		}
		if (indicators.adx().strongTrend()) {
			if (indicators.adx().bullish()) {
				bullish += 15;
				bullishReasons.add("ADX confirms bullish trend");
			}
			if (indicators.adx().bearish()) {
				bearish += 15;
				bearishReasons.add("ADX confirms bearish trend");
			}
		}

		return bullish >= bearish ? new Signal(OptionType.CE, Math.min(bullish, 95), bullishReasons)
				: new Signal(OptionType.PE, Math.min(bearish, 95), bearishReasons);
	}

	private boolean isTradable(OptionQuote quote, BigDecimal spotPrice, RiskProfile riskProfile) {
		if (quote.ltp() == null || quote.ltp().signum() <= 0 || quote.strike() == null) {
			return false;
		}
		if (quote.ltp().compareTo(BigDecimal.valueOf(tradingProperties.getMinimumPremium())) < 0
				|| quote.ltp().compareTo(BigDecimal.valueOf(tradingProperties.getMaximumPremium())) > 0) {
			return false;
		}
		if (strikeDistance(quote, spotPrice)
				.compareTo(BigDecimal.valueOf(tradingProperties.getMaxStrikeDistance())) > 0) {
			return false;
		}
		return switch (riskProfile) {
		case CONSERVATIVE -> isInTheMoney(quote, spotPrice);
		case BALANCED -> isInTheMoney(quote, spotPrice) || isAtTheMoney(quote, spotPrice);
		case AGGRESSIVE -> true;
		};
	}

	private int contractScore(OptionQuote quote, OptionType optionType, BigDecimal spotPrice, RiskProfile riskProfile) {
		int score = 0;
		if (isInTheMoney(quote, spotPrice)) {
			score += riskProfile == RiskProfile.CONSERVATIVE ? 45 : 30;
		} else if (isAtTheMoney(quote, spotPrice)) {
			score += 35;
		} else {
			score += riskProfile == RiskProfile.AGGRESSIVE ? 25 : 0;
		}
		if (strikeDistance(quote, spotPrice).compareTo(BigDecimal.valueOf(tradingProperties.getStrikeStep())) <= 0) {
			score += 30;
		} else {
			score += 10;
		}
		BigDecimal preferredPremium = BigDecimal
				.valueOf((tradingProperties.getMinimumPremium() + tradingProperties.getMaximumPremium()) / 2);
		if (quote.ltp().subtract(preferredPremium).abs().compareTo(BigDecimal.valueOf(100)) <= 0) {
			score += 25;
		}
		return score;
	}

	private boolean isInTheMoney(OptionQuote quote, BigDecimal spotPrice) {
		BigDecimal strike = BigDecimal.valueOf(quote.strike());
		return (quote.optionType() == OptionType.CE && strike.compareTo(spotPrice) < 0)
				|| (quote.optionType() == OptionType.PE && strike.compareTo(spotPrice) > 0);
	}

	private boolean isAtTheMoney(OptionQuote quote, BigDecimal spotPrice) {
		return strikeDistance(quote, spotPrice)
				.compareTo(BigDecimal.valueOf(tradingProperties.getStrikeStep() / 2)) <= 0;
	}

	private BigDecimal strikeDistance(OptionQuote quote, BigDecimal spotPrice) {
		return BigDecimal.valueOf(quote.strike()).subtract(spotPrice).abs();
	}

	private Targets targets(BigDecimal entry, TradingStyle style) {
		return switch (style) {
		case SCALPING -> targetSet(entry, 0.03, 0.10, 0.15, 0.22, 0.30);
		case INTRADAY -> targetSet(entry, 0.03, 0.15, 0.25, 0.40, 0.20);
		case SWING -> targetSet(entry, 0.05, 0.25, 0.45, 0.70, 0.25);
		case POSITIONAL -> targetSet(entry, 0.05, 0.35, 0.65, 1.00, 0.30);
		};
	}

	private Targets targetSet(BigDecimal entry, double range, double t1, double t2, double t3, double stop) {
		return new Targets(level(entry, 1 - range), level(entry, 1 + range), level(entry, 1 - stop),
				level(entry, 1 + t1), level(entry, 1 + t2), level(entry, 1 + t3));
	}

	private int positionLots(Double capital, BigDecimal entry) {
		if (capital == null || capital <= 0 || entry.signum() <= 0) {
			return 1;
		}
		return Math.max(1, Math.min(5, (int) Math.floor(capital / entry.doubleValue() / 100.0)));
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
		case BALANCED -> RiskLevel.MEDIUM;
		case AGGRESSIVE -> RiskLevel.HIGH;
		};
	}

	private TradeRecommendation noTrade(RecommendationRequest request, BigDecimal spotPrice, List<String> reasons,
			String rejectedReason) {

		return TradeRecommendation.builder().action(RecommendationAction.WAIT).instrument(request.instrument())
				.expiryLabel(request.expiryType().name()).spotPrice(spotPrice).confidence(0)
				.risk(riskLevel(request.riskProfile())).quantity(0).holdingTime("No trade")
				.reasons(List.copyOf(reasons)).rejectedReasons(List.of(rejectedReason)).build();
	}

	private List<Candle> historicalCandles(String symbol) {
		LocalDateTime to = LocalDateTime.now();
		try {
			return historicalDataProvider.fetchHistoricalData(instrumentProvider.getInstrumentToken(NSE, symbol),
					symbol, NSE, INTERVAL, to.minusDays(HISTORY_DAYS), to, false, false);
		} catch (Exception | KiteException exception) {
			throw new IllegalStateException("Unable to load Zerodha historical candles for " + symbol, exception);
		}
	}

	private BigDecimal liveSpotPrice(String symbol) {
		BigDecimal ltp = quoteProvider.getLTP(NSE + ":" + symbol);
		if (ltp == null || ltp.signum() <= 0) {
			throw new IllegalStateException("No live index price available for " + symbol);
		}
		return ltp;
	}

	private RecommendationRequest normalize(RecommendationRequest request) {
		if (request == null || request.instrument() == null || request.instrument().isBlank()) {
			throw new IllegalArgumentException("Instrument is required");
		}
		return RecommendationRequest.builder().instrument(optionUnderlying(request.instrument()))
				.expiryType(request.expiryType() == null ? com.banknifty.enums.ExpiryType.WEEKLY : request.expiryType())
				.tradingStyle(request.tradingStyle() == null ? TradingStyle.INTRADAY : request.tradingStyle())
				.riskProfile(request.riskProfile() == null ? RiskProfile.BALANCED : request.riskProfile())
				.capital(request.capital()).build();
	}

	private String spotSymbol(String instrument) {
		return "BANKNIFTY".equalsIgnoreCase(instrument) ? "NIFTY BANK" : instrument;
	}

	private String optionUnderlying(String instrument) {
		String normalized = instrument.trim().toUpperCase();
		return normalized.equals("NIFTY BANK") || normalized.equals("BANK NIFTY") ? "BANKNIFTY" : normalized;
	}

	private BigDecimal level(BigDecimal value, double multiplier) {
		return value.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
	}

	private record Signal(OptionType optionType, int confidence, List<String> reasons) {
	}

	private record Targets(BigDecimal entryMin, BigDecimal entryMax, BigDecimal stopLoss, BigDecimal target1,
			BigDecimal target2, BigDecimal target3) {
	}
}
