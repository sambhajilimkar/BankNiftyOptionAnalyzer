package com.banknifty.service;

import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.indicator.ADXIndicatorEngine;
import com.banknifty.indicator.EMAIndicatorEngine;
import com.banknifty.indicator.MACDIndicatorEngine;
import com.banknifty.indicator.RSIIndicatorEngine;
import com.banknifty.indicator.VWAPIndicatorEngine;
import com.banknifty.model.Candle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendAnalysisService {

	private static final int MINIMUM_CANDLES = 50;
	private static final BigDecimal RSI_BULLISH = BigDecimal.valueOf(55);
	private static final BigDecimal RSI_BEARISH = BigDecimal.valueOf(45);
	private static final BigDecimal STRONG_ADX = BigDecimal.valueOf(20);

	private final EMAIndicatorEngine emaIndicatorEngine;
	private final RSIIndicatorEngine rsiIndicatorEngine;
	private final MACDIndicatorEngine macdIndicatorEngine;
	private final VWAPIndicatorEngine vwapIndicatorEngine;
	private final ADXIndicatorEngine adxIndicatorEngine;

	public TrendAnalysisResult analyze(List<Candle> candles) {
		if (candles == null || candles.size() < MINIMUM_CANDLES) {
			return noTrade("Insufficient historical candles: at least " + MINIMUM_CANDLES + " are required");
		}

		Object emaResult = emaIndicatorEngine.calculate(candles);
		Object rsiResult = rsiIndicatorEngine.calculate(candles);
		Object macdResult = macdIndicatorEngine.calculate(candles);
		Object vwapResult = vwapIndicatorEngine.calculate(candles);
		Object adxResult = adxIndicatorEngine.calculate(candles);

		BigDecimal spotPrice = candles.get(candles.size() - 1).close();
		BigDecimal ema20 = number(emaResult, "ema20", "emaTwenty");
		BigDecimal ema50 = number(emaResult, "ema50", "emaFifty");
		BigDecimal rsi = number(rsiResult, "rsi", "value");
		BigDecimal macd = number(macdResult, "macd", "macdLine", "value");
		BigDecimal vwap = number(vwapResult, "vwap", "value");
		BigDecimal adx = number(adxResult, "adx", "value");

		int score = 0;
		List<String> reasons = new ArrayList<>();

		if (spotPrice.compareTo(ema20) > 0) {
			score += 20;
			reasons.add("Price is above EMA20");
		} else {
			score -= 20;
			reasons.add("Price is below EMA20");
		}

		if (ema20.compareTo(ema50) > 0) {
			score += 20;
			reasons.add("EMA20 is above EMA50");
		} else {
			score -= 20;
			reasons.add("EMA20 is below EMA50");
		}

		if (rsi.compareTo(RSI_BULLISH) >= 0) {
			score += 15;
			reasons.add("RSI is bullish at " + formatted(rsi));
		} else if (rsi.compareTo(RSI_BEARISH) <= 0) {
			score -= 15;
			reasons.add("RSI is bearish at " + formatted(rsi));
		} else {
			reasons.add("RSI is neutral at " + formatted(rsi));
		}

		if (macd.signum() > 0) {
			score += 15;
			reasons.add("MACD is positive");
		} else if (macd.signum() < 0) {
			score -= 15;
			reasons.add("MACD is negative");
		}

		if (spotPrice.compareTo(vwap) > 0) {
			score += 15;
			reasons.add("Price is above VWAP");
		} else {
			score -= 15;
			reasons.add("Price is below VWAP");
		}

		if (adx.compareTo(STRONG_ADX) >= 0) {
			score += score >= 0 ? 15 : -15;
			reasons.add("ADX confirms a tradable trend at " + formatted(adx));
		} else {
			reasons.add("ADX is weak at " + formatted(adx));
		}

		RecommendationAction action;
		OptionType optionType;
		int confidence;
		if (score >= 50) {
			action = RecommendationAction.BUY;
			optionType = OptionType.CE;
			confidence = Math.min(95, 50 + score / 2);
		} else if (score <= -50) {
			action = RecommendationAction.SELL;
			optionType = OptionType.PE;
			confidence = Math.min(95, 50 + Math.abs(score) / 2);
		} else {
			action = RecommendationAction.SIDEWAYS;
			optionType = OptionType.CE;
			confidence = Math.max(20, 45 - Math.abs(score) / 2);
			reasons.add("Signals are not aligned; no directional option trade");
		}

		return TrendAnalysisResult.builder().action(action).optionType(optionType).spotPrice(spotPrice)
				.confidence(confidence).ema20(ema20).ema50(ema50).rsi(rsi).macd(macd).adx(adx).vwap(vwap)
				.reasons(List.copyOf(reasons)).build();
	}

	private TrendAnalysisResult noTrade(String reason) {
		return TrendAnalysisResult.builder().action(RecommendationAction.SIDEWAYS).optionType(OptionType.CE)
				.spotPrice(BigDecimal.ZERO).confidence(0).ema20(BigDecimal.ZERO).ema50(BigDecimal.ZERO)
				.rsi(BigDecimal.ZERO).macd(BigDecimal.ZERO).adx(BigDecimal.ZERO).vwap(BigDecimal.ZERO)
				.reasons(List.of(reason)).build();
	}

	private BigDecimal number(Object result, String... accessors) {
		for (String accessor : accessors) {
			try {
				Method method = result.getClass().getMethod(accessor);
				Object value = method.invoke(result);
				if (value instanceof BigDecimal decimal) {
					return decimal;
				}
				if (value instanceof Number number) {
					return BigDecimal.valueOf(number.doubleValue());
				}
			} catch (ReflectiveOperationException ignored) {
				// Try the next result accessor supported by the current engine result.
			}
		}
		throw new IllegalStateException("Unable to read indicator value from " + result.getClass().getName());
	}

	private String formatted(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
