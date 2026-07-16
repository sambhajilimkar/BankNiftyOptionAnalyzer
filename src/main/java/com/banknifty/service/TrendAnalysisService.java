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

	private static final int BUY_THRESHOLD = 75;

	private static final int SELL_THRESHOLD = -75;

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
		 * ===================================================== EMA (30)
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
		 * ===================================================== RSI (15)
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
		 * ===================================================== MACD (25)
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
		 * ===================================================== VWAP (15)
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
		 * Bonus confirmation
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
		 * ===================================================== ADX (15)
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
		 * ===================================================== OPEN INTEREST (15)
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
		 * ===================================================== ADX + OI Confirmation
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
		 * (10) =====================================================
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
		 * ===================================================== PIVOT / CPR (5)
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
		 * ===================================================== FINAL DECISION ENGINE
		 * =====================================================
		 */

		confidence = Math.min(100, Math.max(confidence, Math.abs(score)));

		RecommendationAction action;
		OptionType optionType;

		boolean bullishTrade = score >= 55 && confidence >= 60;

		boolean bearishTrade = score <= -55 && confidence >= 60;

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
			optionType = score >= 0 ? OptionType.CE : OptionType.PE;

			if (Math.abs(score) >= 40) {

				reasons.add("Trend forming - wait for confirmation");

			} else {

				reasons.add("Sideways / weak trend");

			}

		}

		return TrendAnalysisResult.builder()

				.action(action)

				.optionType(optionType)

				.spotPrice(spotPrice)

				.confidence(confidence)

				.ema20(ema.ema20())

				.ema50(ema.ema50())

				.rsi(rsi.rsi())

				.macd(macd.macd())

				.adx(BigDecimal.valueOf(adx.adx()))

				.vwap(vwap.vwap())

				.reasons(List.copyOf(reasons))

				.build();

	}

	private TrendAnalysisResult noTrade(String reason) {

		return TrendAnalysisResult.builder()

				.action(RecommendationAction.WAIT)

				.optionType(OptionType.CE)

				.spotPrice(BigDecimal.ZERO)

				.confidence(0)

				.ema20(BigDecimal.ZERO)

				.ema50(BigDecimal.ZERO)

				.rsi(BigDecimal.ZERO)

				.macd(BigDecimal.ZERO)

				.adx(BigDecimal.ZERO)

				.vwap(BigDecimal.ZERO)

				.reasons(List.of(reason))

				.build();

	}

}