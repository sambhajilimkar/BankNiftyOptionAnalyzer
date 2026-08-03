package com.banknifty.market.regime;

import com.banknifty.indicator.result.ADXResult;
import com.banknifty.indicator.result.ATRResult;
import com.banknifty.indicator.result.EMAResult;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.indicator.result.MACDResult;
import com.banknifty.indicator.result.RSIResult;
import com.banknifty.indicator.result.SuperTrendResult;
import com.banknifty.indicator.result.VWAPResult;
import com.banknifty.market.context.MarketContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DefaultMarketRegimeEngine implements MarketRegimeEngine {

	private static final int MIN_TREND_SCORE = 55;
	private static final int MIN_DIRECTIONAL_SCORE = 25;

	@Override
	public MarketRegimeResult detect(IndicatorSnapshot indicators, MarketContext marketContext) {

		List<String> reasons = new ArrayList<>();

		if (indicators == null) {
			return unknown(false, 0, "Indicator snapshot unavailable");
		}

		/*
		 * Market-context restrictions have the highest priority.
		 */
		if (marketContext != null && !marketContext.tradeAllowed()) {

			reasons.add("Trading blocked by market context");

			if (marketContext.warnings() != null) {
				reasons.addAll(marketContext.warnings());
			}

			return MarketRegimeResult.builder().regime(MarketRegime.UNKNOWN).confidence(100).tradeAllowed(false)
					.reasons(reasons).build();
		}

		/*
		 * Event-driven session.
		 *
		 * We intentionally do not infer direction here. Event days can invalidate
		 * normal technical behaviour.
		 */
		if (marketContext != null && marketContext.eventDay()) {

			reasons.add("Event-driven market conditions");

			if (marketContext.warnings() != null) {
				reasons.addAll(marketContext.warnings());
			}

			return MarketRegimeResult.builder().regime(MarketRegime.EVENT_DRIVEN)
					.confidence(adjustConfidence(80, marketContext)).tradeAllowed(marketContext.tradeAllowed())
					.reasons(reasons).build();
		}

		/*
		 * Expiry-day regime.
		 *
		 * Expiry behaviour can materially change option pricing, gamma exposure and
		 * intraday volatility.
		 */
		if (marketContext != null && marketContext.expiryDay()) {

			reasons.add("BankNifty expiry-day conditions");

			if (marketContext.weeklyExpiry()) {
				reasons.add("Weekly expiry");
			}

			if (marketContext.monthlyExpiry()) {
				reasons.add("Monthly expiry");
			}

			return MarketRegimeResult.builder().regime(MarketRegime.EXPIRY_DAY)
					.confidence(adjustConfidence(85, marketContext)).tradeAllowed(marketContext.tradeAllowed())
					.reasons(reasons).build();
		}

		int bullishScore = 0;
		int bearishScore = 0;
		int trendStrengthScore = 0;

		EMAResult ema = indicators.ema();
		RSIResult rsi = indicators.rsi();
		MACDResult macd = indicators.macd();
		ADXResult adx = indicators.adx();
		VWAPResult vwap = indicators.vwap();
		ATRResult atr = indicators.atr();
		SuperTrendResult superTrend = indicators.superTrend();

		/*
		 * EMA structure.
		 */
		if (ema != null) {

			if (ema.bullishAlignment()) {
				bullishScore += 25;
				trendStrengthScore += 15;
				reasons.add("Bullish EMA alignment");
			}

			if (ema.bearishAlignment()) {
				bearishScore += 25;
				trendStrengthScore += 15;
				reasons.add("Bearish EMA alignment");
			}

			if (ema.rising()) {
				bullishScore += 10;
				reasons.add("EMA trend rising");
			}

			if (ema.falling()) {
				bearishScore += 10;
				reasons.add("EMA trend falling");
			}

			if (ema.bullishCross()) {
				bullishScore += 10;
				reasons.add("Bullish EMA crossover");
			}

			if (ema.bearishCross()) {
				bearishScore += 10;
				reasons.add("Bearish EMA crossover");
			}
		}

		/*
		 * ADX / directional movement.
		 *
		 * ADX contributes mainly to trend strength. +DI/-DI contributes to direction.
		 */
		if (adx != null) {

			if (adx.strongTrend()) {
				trendStrengthScore += 25;
				reasons.add("ADX confirms strong trend");
			}

			if (adx.bullish() || adx.plusDI() > adx.minusDI()) {
				bullishScore += 15;
				reasons.add("+DI dominates -DI");
			}

			if (adx.bearish() || adx.minusDI() > adx.plusDI()) {
				bearishScore += 15;
				reasons.add("-DI dominates +DI");
			}
		}

		/*
		 * MACD momentum.
		 */
		if (macd != null) {

			if (macd.bullish()) {
				bullishScore += 15;
				reasons.add("MACD bullish momentum");
			}

			if (macd.bearish()) {
				bearishScore += 15;
				reasons.add("MACD bearish momentum");
			}

			if (macd.bullishCross()) {
				bullishScore += 5;
				reasons.add("MACD bullish crossover");
			}

			if (macd.bearishCross()) {
				bearishScore += 5;
				reasons.add("MACD bearish crossover");
			}
		}

		/*
		 * VWAP position.
		 */
		if (vwap != null) {

			if (vwap.aboveVWAP()) {
				bullishScore += 10;
				reasons.add("Price above VWAP");
			} else {
				bearishScore += 10;
				reasons.add("Price below VWAP");
			}
		}

		/*
		 * SuperTrend.
		 */
		if (superTrend != null) {

			if (superTrend.bullish()) {
				bullishScore += 15;
				trendStrengthScore += 5;
				reasons.add("SuperTrend bullish");
			}

			if (superTrend.bearish()) {
				bearishScore += 15;
				trendStrengthScore += 5;
				reasons.add("SuperTrend bearish");
			}
		}

		/*
		 * RSI is intentionally used as confirmation rather than as the primary trend
		 * detector.
		 */
		if (rsi != null) {

			if (rsi.bullish() && !rsi.overBought()) {
				bullishScore += 10;
				reasons.add("RSI confirms bullish momentum");
			}

			if (rsi.bearish() && !rsi.overSold()) {
				bearishScore += 10;
				reasons.add("RSI confirms bearish momentum");
			}

			if (rsi.overBought()) {
				reasons.add("RSI overbought");
			}

			if (rsi.overSold()) {
				reasons.add("RSI oversold");
			}
		}

		/*
		 * Global market context is deliberately a small adjustment. Local BankNifty
		 * technical structure remains dominant.
		 */
		if (marketContext != null) {

			if (marketContext.globalBullish()) {
				bullishScore += 5;
				reasons.add("Global market context bullish");
			}

			if (marketContext.globalBearish()) {
				bearishScore += 5;
				reasons.add("Global market context bearish");
			}
		}

		int directionalScore = bullishScore - bearishScore;

		/*
		 * Strong directional regime.
		 */
		if (trendStrengthScore >= MIN_TREND_SCORE && directionalScore >= MIN_DIRECTIONAL_SCORE) {

			int confidence = calculateTrendConfidence(bullishScore, bearishScore, trendStrengthScore, marketContext);

			log.debug("Market regime TRENDING_BULLISH: bullish={}, bearish={}, strength={}, confidence={}",
					bullishScore, bearishScore, trendStrengthScore, confidence);

			return MarketRegimeResult.builder().regime(MarketRegime.TRENDING_BULLISH).confidence(confidence)
					.tradeAllowed(isTradeAllowed(marketContext)).reasons(reasons).build();
		}

		if (trendStrengthScore >= MIN_TREND_SCORE && directionalScore <= -MIN_DIRECTIONAL_SCORE) {

			int confidence = calculateTrendConfidence(bearishScore, bullishScore, trendStrengthScore, marketContext);

			log.debug("Market regime TRENDING_BEARISH: bullish={}, bearish={}, strength={}, confidence={}",
					bullishScore, bearishScore, trendStrengthScore, confidence);

			return MarketRegimeResult.builder().regime(MarketRegime.TRENDING_BEARISH).confidence(confidence)
					.tradeAllowed(isTradeAllowed(marketContext)).reasons(reasons).build();
		}

		/*
		 * Volatility regimes.
		 *
		 * These are evaluated after strong trends because a trending market may also
		 * naturally have elevated ATR.
		 */
		if (isHighVolatility(atr, marketContext)) {

			reasons.add("Elevated volatility detected");

			return MarketRegimeResult.builder().regime(MarketRegime.HIGH_VOLATILITY)
					.confidence(adjustConfidence(75, marketContext)).tradeAllowed(isTradeAllowed(marketContext))
					.reasons(reasons).build();
		}

		if (isLowVolatility(atr)) {

			reasons.add("Low volatility detected");

			return MarketRegimeResult.builder().regime(MarketRegime.LOW_VOLATILITY)
					.confidence(adjustConfidence(75, marketContext)).tradeAllowed(isTradeAllowed(marketContext))
					.reasons(reasons).build();
		}

		/*
		 * Weak ADX + conflicting/weak direction is treated as range-bound.
		 */
		boolean weakTrend = adx == null || !adx.strongTrend();

		if (weakTrend && Math.abs(directionalScore) < MIN_DIRECTIONAL_SCORE) {

			reasons.add("No strong directional trend");
			reasons.add("Bullish and bearish signals are balanced");

			int confidence = Math.min(90, 65 + Math.max(0, MIN_DIRECTIONAL_SCORE - Math.abs(directionalScore)));

			return MarketRegimeResult.builder().regime(MarketRegime.RANGE_BOUND)
					.confidence(adjustConfidence(confidence, marketContext)).tradeAllowed(isTradeAllowed(marketContext))
					.reasons(reasons).build();
		}

		/*
		 * Do not guess BREAKOUT/BREAKDOWN/REVERSAL here.
		 *
		 * Those regimes require structural price context such as support/resistance and
		 * previous-candle behaviour.
		 */
		reasons.add("Market regime is not sufficiently confirmed");

		return MarketRegimeResult.builder().regime(MarketRegime.UNKNOWN).confidence(adjustConfidence(40, marketContext))
				.tradeAllowed(isTradeAllowed(marketContext)).reasons(reasons).build();
	}

	private int calculateTrendConfidence(int dominantScore, int oppositeScore, int trendStrengthScore,
			MarketContext marketContext) {

		int separation = Math.max(0, dominantScore - oppositeScore);

		int confidence = 45 + Math.min(30, separation / 2) + Math.min(20, trendStrengthScore / 4);

		return adjustConfidence(confidence, marketContext);
	}

	private boolean isHighVolatility(ATRResult atr, MarketContext marketContext) {

		if (marketContext != null && marketContext.highVolatility()) {
			return true;
		}

		return atr != null && atr.highVolatility();
	}

	private boolean isLowVolatility(ATRResult atr) {
		return atr != null && atr.lowVolatility();
	}

	private boolean isTradeAllowed(MarketContext marketContext) {
		return marketContext == null || marketContext.tradeAllowed();
	}

	private int adjustConfidence(int confidence, MarketContext marketContext) {

		int adjusted = confidence;

		if (marketContext != null) {
			adjusted += marketContext.confidenceAdjustment();
		}

		return Math.max(0, Math.min(100, adjusted));
	}

	private MarketRegimeResult unknown(boolean tradeAllowed, int confidence, String reason) {

		return MarketRegimeResult.builder().regime(MarketRegime.UNKNOWN).confidence(confidence)
				.tradeAllowed(tradeAllowed).reasons(List.of(reason)).build();
	}
}