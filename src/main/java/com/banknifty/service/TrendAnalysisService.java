package com.banknifty.service;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.indicator.ADXIndicatorEngine;
import com.banknifty.indicator.EMAIndicatorEngine;
import com.banknifty.indicator.MACDIndicatorEngine;
import com.banknifty.indicator.RSIIndicatorEngine;
import com.banknifty.indicator.VWAPIndicatorEngine;
import com.banknifty.indicator.result.ADXResult;
import com.banknifty.indicator.result.EMAResult;
import com.banknifty.indicator.result.MACDResult;
import com.banknifty.indicator.result.RSIResult;
import com.banknifty.indicator.result.VWAPResult;
import com.banknifty.model.Candle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendAnalysisService {

	private static final int MINIMUM_CANDLES = 50;

	/*
	 * Keep the existing effective thresholds for now.
	 *
	 * Previously these constants were 75 / -75 while the actual decision logic used
	 * hard-coded 55 / -55.
	 *
	 * We intentionally preserve the current live behaviour and remove that
	 * inconsistency.
	 */
	private static final int BUY_THRESHOLD = 55;

	private static final int SELL_THRESHOLD = -55;

	private static final int MINIMUM_CONFIDENCE = 60;

	private static final int FORMING_TREND_THRESHOLD = 40;

	private final EMAIndicatorEngine emaIndicatorEngine;

	private final RSIIndicatorEngine rsiIndicatorEngine;

	private final MACDIndicatorEngine macdIndicatorEngine;

	private final VWAPIndicatorEngine vwapIndicatorEngine;

	private final ADXIndicatorEngine adxIndicatorEngine;

	private final OpenInterestAnalysisService openInterestAnalysisService;

	private final SupportResistanceService supportResistanceService;

	private final PivotService pivotService;

	public TrendAnalysisResult analyze(List<Candle> candles) {

		if (candles == null || candles.size() < MINIMUM_CANDLES) {
			return noTrade("Insufficient candles");
		}

		EMAResult ema = emaIndicatorEngine.calculate(candles);

		RSIResult rsi = rsiIndicatorEngine.calculate(candles);

		MACDResult macd = macdIndicatorEngine.calculate(candles);

		VWAPResult vwap = vwapIndicatorEngine.calculate(candles);

		ADXResult adx = adxIndicatorEngine.calculate(candles);

		OpenInterestAnalysisService.OpenInterestResult oi = openInterestAnalysisService.analyze(candles);

		SupportResistanceResult sr = supportResistanceService.calculate(candles);

		PivotResult pivot = pivotService.calculate(candles);

		BigDecimal spotPrice = candles.get(candles.size() - 1).close();

		int score = 0;

		int confidence = 0;

		List<String> reasons = new ArrayList<>();

		/*
		 * ===================================================== EMA
		 * =====================================================
		 */
		if (ema.bullishCross()) {

			score += 30;
			confidence += 25;

			reasons.add("Strong EMA Bullish Crossover");

		} else if (ema.bearishCross()) {

			score -= 30;
			confidence += 25;

			reasons.add("Strong EMA Bearish Crossover");

		} else if (ema.bullishAlignment()) {

			score += 20;
			confidence += 18;

			reasons.add("EMA Bullish Alignment");

		} else if (ema.bearishAlignment()) {

			score -= 20;
			confidence += 18;

			reasons.add("EMA Bearish Alignment");
		}

		/*
		 * ===================================================== RSI
		 * =====================================================
		 */
		if (rsi.bullish()) {

			score += 10;
			confidence += 10;

			reasons.add("RSI Bullish");

		} else if (rsi.bearish()) {

			score -= 10;
			confidence += 10;

			reasons.add("RSI Bearish");

		} else {

			reasons.add("RSI Neutral");
		}

		if (rsi.rising()) {

			score += 5;
			confidence += 2;

			reasons.add("RSI Rising Momentum");
		}

		if (rsi.falling()) {

			score -= 5;
			confidence += 2;

			reasons.add("RSI Falling Momentum");
		}

		/*
		 * ===================================================== MACD
		 * =====================================================
		 */
		if (macd.bullishCross()) {

			score += 25;
			confidence += 20;

			reasons.add("Strong MACD Bullish Cross");

		} else if (macd.bearishCross()) {

			score -= 25;
			confidence += 20;

			reasons.add("Strong MACD Bearish Cross");

		} else if (macd.bullish()) {

			score += 15;
			confidence += 12;

			reasons.add("MACD Bullish");

		} else if (macd.bearish()) {

			score -= 15;
			confidence += 12;

			reasons.add("MACD Bearish");
		}

		/*
		 * ===================================================== VWAP
		 * =====================================================
		 */
		if (vwap.priceAboveVWAP()) {

			score += 15;
			confidence += 10;

			reasons.add("Price Above VWAP");

		} else {

			score -= 15;
			confidence += 10;

			reasons.add("Price Below VWAP");
		}

		/*
		 * MACD + VWAP confirmation.
		 */
		if (macd.bullish() && vwap.priceAboveVWAP()) {

			score += 5;
			confidence += 3;

			reasons.add("MACD + VWAP Bullish Confirmation");
		}

		if (macd.bearish() && !vwap.priceAboveVWAP()) {

			score -= 5;
			confidence += 3;

			reasons.add("MACD + VWAP Bearish Confirmation");
		}

		/*
		 * ===================================================== ADX
		 * =====================================================
		 */
		if (adx.trending()) {

			confidence += 15;

			reasons.add("Strong ADX Trend");

			if (adx.bullish()) {

				score += 15;

				reasons.add("ADX Bullish");

			} else if (adx.bearish()) {

				score -= 15;

				reasons.add("ADX Bearish");
			}

		} else {

			score -= 5;
			confidence -= 2;

			reasons.add("Weak ADX - Sideways Market");
		}

		/*
		 * ===================================================== OPEN INTEREST
		 * =====================================================
		 */
		switch (oi.trend()) {

		case LONG_BUILDUP -> {

			score += 15;
			confidence += 10;

			reasons.add("OI Long Build-up");
		}

		case SHORT_BUILDUP -> {

			score -= 15;
			confidence += 10;

			reasons.add("OI Short Build-up");
		}

		case SHORT_COVERING -> {

			score += 8;
			confidence += 5;

			reasons.add("OI Short Covering");
		}

		case LONG_UNWINDING -> {

			score -= 8;
			confidence += 5;

			reasons.add("OI Long Unwinding");
		}

		default -> reasons.add("Neutral Open Interest");
		}

		/*
		 * ===================================================== ADX + OI CONFIRMATION
		 * =====================================================
		 */
		if (adx.trending() && oi.trend() == OpenInterestAnalysisService.OpenInterestTrend.LONG_BUILDUP) {

			score += 5;
			confidence += 3;

			reasons.add("ADX + OI Bullish Confirmation");
		}

		if (adx.trending() && oi.trend() == OpenInterestAnalysisService.OpenInterestTrend.SHORT_BUILDUP) {

			score -= 5;
			confidence += 3;

			reasons.add("ADX + OI Bearish Confirmation");
		}

		/*
		 * ===================================================== SUPPORT / RESISTANCE
		 * =====================================================
		 */
		if (sr.breakout()) {

			score += 10;
			confidence += 10;

			reasons.add("Resistance Breakout");

		} else if (sr.breakdown()) {

			score -= 10;
			confidence += 10;

			reasons.add("Support Breakdown");

		} else {

			if (sr.nearSupport()) {

				score += 5;

				reasons.add("Trading Near Support");
			}

			if (sr.nearResistance()) {

				score -= 5;

				reasons.add("Trading Near Resistance");
			}
		}

		/*
		 * ===================================================== PIVOT / CPR
		 * =====================================================
		 */
		if (pivot.bullish()) {

			score += 5;
			confidence += 5;

			reasons.add("Bullish Pivot");

		} else if (pivot.bearish()) {

			score -= 5;
			confidence += 5;

			reasons.add("Bearish Pivot");
		}

		if (pivot.narrowCPR()) {
			reasons.add("Narrow CPR");
		}

		/*
		 * ===================================================== FINAL TECHNICAL
		 * CONFIDENCE =====================================================
		 *
		 * Preserve existing behaviour for now.
		 *
		 * Later, final trade confidence will be calculated from:
		 *
		 * technical confidence + setup confidence + option-chain confidence + execution
		 * quality + risk/reward quality
		 */
		confidence = Math.min(100, Math.max(confidence, Math.abs(score)));

		RecommendationAction action;

		OptionType optionType;

		boolean bullishTrade = score >= BUY_THRESHOLD && confidence >= MINIMUM_CONFIDENCE;

		boolean bearishTrade = score <= SELL_THRESHOLD && confidence >= MINIMUM_CONFIDENCE;

		if (bullishTrade) {

			action = RecommendationAction.BUY;

			optionType = OptionType.CE;

			reasons.add("Bullish trend confirmed");

		} else if (bearishTrade) {

			action = RecommendationAction.BUY;

			optionType = OptionType.PE;

			reasons.add("Bearish trend confirmed");

		} else {

			action = RecommendationAction.WAIT;

			/*
			 * optionType here represents technical directional bias. It does NOT mean that
			 * a trade should be executed.
			 */
			optionType = score >= 0 ? OptionType.CE : OptionType.PE;

			if (Math.abs(score) >= FORMING_TREND_THRESHOLD) {

				reasons.add("Trend forming - wait for confirmation");

			} else {

				reasons.add("Sideways / weak trend");
			}
		}

		/*
		 * ===================================================== RESULT
		 * =====================================================
		 *
		 * Structural information is now exposed for the Trade Setup Engine.
		 */
		return TrendAnalysisResult.builder()

				.action(action)

				.optionType(optionType)

				.spotPrice(spotPrice)

				.confidence(confidence)

				.technicalScore(score)

				.ema20(ema.ema20())

				.ema50(ema.ema50())

				.rsi(rsi.rsi())

				.macd(macd.macd())

				.adx(BigDecimal.valueOf(adx.adx()))

				.vwap(vwap.vwap())

				.supportResistance(sr)

				.pivot(pivot)

				.openInterest(oi)

				.reasons(List.copyOf(reasons))

				.build();
	}

	private TrendAnalysisResult noTrade(String reason) {

		return TrendAnalysisResult.builder()

				.action(RecommendationAction.WAIT)

				.optionType(OptionType.CE)

				.spotPrice(BigDecimal.ZERO)

				.confidence(0)

				.technicalScore(0)

				.ema20(BigDecimal.ZERO)

				.ema50(BigDecimal.ZERO)

				.rsi(BigDecimal.ZERO)

				.macd(BigDecimal.ZERO)

				.adx(BigDecimal.ZERO)

				.vwap(BigDecimal.ZERO)

				.supportResistance(null)

				.pivot(null)

				.openInterest(null)

				.reasons(List.of(reason))

				.build();
	}
}