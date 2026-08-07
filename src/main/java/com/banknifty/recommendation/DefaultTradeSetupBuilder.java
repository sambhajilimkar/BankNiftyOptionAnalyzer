package com.banknifty.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.enums.OptionType;
import com.banknifty.model.Candle;
import com.banknifty.recommendation.model.DecisionContext;
import com.banknifty.recommendation.model.SetupType;
import com.banknifty.recommendation.model.StrikeCandidate;
import com.banknifty.recommendation.model.TradeSetup;
import com.banknifty.service.TrendAnalysisResult;

/**
 * Detects the underlying setup and scores it.
 *
 * IMPORTANT: This class no longer decides BUY / WAIT. It only evaluates the
 * quality of the setup.
 *
 * DefaultRecommendationEngine is the final authority for trade execution.
 */
@Service
public class DefaultTradeSetupBuilder implements TradeSetupBuilder {

	@Override
	public TradeSetup build(DecisionContext context, StrikeCandidate candidate) {

		List<String> reasons = new ArrayList<>();
		List<String> rejected = new ArrayList<>();

		if (context == null || candidate == null || candidate.ltp() == null || candidate.ltp().signum() <= 0) {

			return rejected(candidate, SetupType.NONE, reasons, List.of("Missing market or contract data"), 0, 0);
		}

		TrendAnalysisResult trend = context.trendAnalysis();

		if (trend == null) {

			return rejected(candidate, SetupType.NONE, reasons, List.of("Market structure unavailable"), 0, 0);
		}

		/*
		 * -------------------------------------------------------- Market Context
		 * --------------------------------------------------------
		 *
		 * Do NOT reject. Simply reduce setup score slightly.
		 */
		int scorePenalty = 0;

		if (!context.tradeAllowed()) {

			scorePenalty += 5;

			reasons.add("Market context is defensive");
		}

		/*
		 * -------------------------------------------------------- Detect setup
		 * --------------------------------------------------------
		 */
		SetupType type = setupType(context, trend, reasons);

		int score = setupScore(type, trend, context, reasons);

		score = Math.max(0, score - scorePenalty);

		/*
		 * -------------------------------------------------------- Secondary
		 * confirmations --------------------------------------------------------
		 */
		SetupSignals signals = setupSignals(context.candles(), trend, reasons);

		if (signals.fakeBreakout()) {

			score = Math.max(0, score - 10);

			reasons.add("Possible fake breakout");
		}

		if (signals.openingRangeBreakout()) {

			score = Math.min(100, score + 10);

			reasons.add("Opening range breakout confirmed");
		}

		if (signals.volumeBreakout()) {

			score = Math.min(100, score + 10);

			reasons.add("Volume breakout confirmed");
		}

		if (signals.cprBreakout()) {

			score = Math.min(100, score + 5);

			reasons.add("CPR breakout confirmed");
		}

		if (signals.multiTimeFrameConfirmed()) {

			score = Math.min(100, score + 10);

			reasons.add("Multi-timeframe confirmation");
		}

		/*
		 * Direction
		 */
		boolean directionMatches = (bullish(type) && candidate.optionType() == OptionType.CE)

				||

				(bearish(type) && candidate.optionType() == OptionType.PE);

		if (type == SetupType.NONE) {

			rejected.add("No confirmed setup");
		}

		if (type != SetupType.NONE && !directionMatches) {

			rejected.add("Option direction does not match setup");
		}

		/*
		 * Lower setup threshold.
		 */
		if (score < 50) {

			rejected.add("Setup score below minimum");
		}

		int confidence = Math.min(100,
				Math.max(0, (int) Math.round(score * 0.70 + context.technicalConfidence() * 0.30)));

		/*
		 * Validity depends only on setup quality.
		 */
		boolean valid = score >= 50;

		return build(candidate, type, valid, score, confidence, reasons, rejected);
	}

	private SetupType setupType(DecisionContext context, TrendAnalysisResult trend, List<String> reasons) {

		if (trend.breakout() && trend.bullishBias()) {

			reasons.add("Resistance breakout");

			return SetupType.BULLISH_BREAKOUT;
		}

		if (trend.breakdown() && trend.bearishBias()) {

			reasons.add("Support breakdown");

			return SetupType.BEARISH_BREAKDOWN;
		}

		if (context.indicators() != null && context.indicators().vwap() != null
				&& context.indicators().vwap().pullback()) {

			if (trend.bullishBias()) {

				reasons.add("Bullish VWAP pullback");

				return SetupType.BULLISH_PULLBACK;
			}

			if (trend.bearishBias()) {

				reasons.add("Bearish VWAP pullback");

				return SetupType.BEARISH_PULLBACK;
			}
		}

		if (context.indicators() != null && context.indicators().vwap() != null
				&& context.indicators().vwap().breakout() && trend.bullishBias()) {

			reasons.add("VWAP bullish reclaim");

			return SetupType.VWAP_BULLISH_RECLAIM;
		}

		if (context.indicators() != null && context.indicators().vwap() != null
				&& !context.indicators().vwap().aboveVWAP() && trend.bearishBias()) {

			reasons.add("VWAP bearish rejection");

			return SetupType.VWAP_BEARISH_REJECTION;
		}

		return SetupType.NONE;
	}

	private int setupScore(SetupType type, TrendAnalysisResult trend, DecisionContext context, List<String> reasons) {

		int base = switch (type) {

		case BULLISH_BREAKOUT, BEARISH_BREAKDOWN -> 75;

		case BULLISH_PULLBACK, BEARISH_PULLBACK, VWAP_BULLISH_RECLAIM, VWAP_BEARISH_REJECTION -> 65;

		case BULLISH_REVERSAL, BEARISH_REVERSAL -> 60;

		case NONE -> 0;
		};

		if (trend.confidence() != null && trend.confidence() >= 60) {

			base += 10;

			reasons.add("Strong technical confirmation");
		}

		return Math.min(base, 100);
	}

	private SetupSignals setupSignals(List<Candle> candles, TrendAnalysisResult trend, List<String> reasons) {

		if (candles == null || candles.size() < 25) {
			return new SetupSignals(false, false, false, false, false);
		}

		List<Candle> ordered = candles.stream().filter(c -> c.close() != null && c.high() != null && c.low() != null)
				.sorted(Comparator.comparing(Candle::dateTime)).toList();

		if (ordered.size() < 25) {
			return new SetupSignals(false, false, false, false, false);
		}

		Candle last = ordered.getLast();

		List<Candle> today = ordered.stream().filter(c -> c.tradeDate().equals(last.tradeDate())).toList();

		boolean openingRangeBreakout = false;
		boolean fakeBreakout = false;

		if (today.size() >= 4) {

			List<Candle> opening = today.stream().limit(3).toList();

			BigDecimal openingHigh = opening.stream().map(Candle::high).max(Comparator.naturalOrder())
					.orElse(last.high());

			BigDecimal openingLow = opening.stream().map(Candle::low).min(Comparator.naturalOrder()).orElse(last.low());

			openingRangeBreakout = (trend.bullishBias() && last.close().compareTo(openingHigh) > 0)
					|| (trend.bearishBias() && last.close().compareTo(openingLow) < 0);

			/*
			 * Softer fake breakout detection.
			 */

			if (openingRangeBreakout) {

				BigDecimal midpoint = openingHigh.add(openingLow).divide(BigDecimal.valueOf(2));

				fakeBreakout = trend.bullishBias() ? last.close().compareTo(midpoint) < 0
						: last.close().compareTo(midpoint) > 0;
			}
		}

		long averageVolume = ordered.subList(Math.max(0, ordered.size() - 21), ordered.size() - 1).stream()
				.map(Candle::volume).filter(v -> v != null).mapToLong(Long::longValue).sum() / 20;

		/*
		 * Relaxed volume confirmation
		 */

		boolean volumeBreakout = last.volume() != null && averageVolume > 0 && last.volume() >= averageVolume * 12 / 10;

		boolean cprBreakout = trend.pivot() != null && trend.pivot().tc() != null && trend.pivot().bc() != null
				&& ((trend.bullishBias() && last.close().compareTo(trend.pivot().tc()) > 0)

						||

						(trend.bearishBias() && last.close().compareTo(trend.pivot().bc()) < 0));

		boolean mtf = aligned(ordered, 1, trend.bullishBias()) && aligned(ordered, 3, trend.bullishBias())
				&& aligned(ordered, 12, trend.bullishBias());

		return new SetupSignals(openingRangeBreakout, fakeBreakout, volumeBreakout, cprBreakout, mtf);
	}

	private boolean aligned(List<Candle> candles, int step, boolean bullish) {

		int count = Math.min(10, candles.size() / step);

		if (count < 3) {
			return false;
		}

		List<BigDecimal> closes = new ArrayList<>();

		for (int i = candles.size() - count * step; i < candles.size(); i += step) {

			closes.add(candles.get(Math.min(i + step - 1, candles.size() - 1)).close());
		}

		BigDecimal average = closes.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(closes.size()), 4, RoundingMode.HALF_UP);

		return bullish ? closes.getLast().compareTo(average) > 0 : closes.getLast().compareTo(average) < 0;
	}

	private TradeSetup build(StrikeCandidate candidate, SetupType type, boolean valid, int score, int confidence,
			List<String> reasons, List<String> rejected) {

		BigDecimal entry = candidate.ltp().setScale(2, RoundingMode.HALF_UP);

		BigDecimal stop = entry.multiply(BigDecimal.valueOf(.85)).setScale(2, RoundingMode.HALF_UP);

		BigDecimal target1 = entry.multiply(BigDecimal.valueOf(1.20)).setScale(2, RoundingMode.HALF_UP);

		BigDecimal target2 = entry.multiply(BigDecimal.valueOf(1.40)).setScale(2, RoundingMode.HALF_UP);

		BigDecimal target3 = entry.multiply(BigDecimal.valueOf(1.60)).setScale(2, RoundingMode.HALF_UP);

		BigDecimal rr = target1.subtract(entry).divide(entry.subtract(stop), 2, RoundingMode.HALF_UP);

		return TradeSetup.builder().setupType(type).valid(valid).setupScore(score).confidence(confidence)
				.tradingSymbol(candidate.tradingSymbol()).strike(candidate.strike())
				.optionType(candidate.optionType().name()).optionPrice(entry).entry(entry).stopLoss(stop)
				.target1(target1).target2(target2).target3(target3).riskReward(rr).reasons(List.copyOf(reasons))
				.rejectedReasons(List.copyOf(rejected)).build();
	}

	private TradeSetup rejected(StrikeCandidate candidate, SetupType type, List<String> reasons, List<String> rejected,
			int score, int confidence) {

		if (candidate == null || candidate.ltp() == null || candidate.ltp().signum() <= 0) {

			return TradeSetup.builder().setupType(type).valid(false).setupScore(score).confidence(confidence)
					.reasons(reasons).rejectedReasons(rejected).build();
		}

		return build(candidate, type, false, score, confidence, reasons, rejected);
	}

	private boolean bullish(SetupType type) {

		return switch (type) {

		case BULLISH_BREAKOUT, BULLISH_PULLBACK, VWAP_BULLISH_RECLAIM, BULLISH_REVERSAL -> true;

		default -> false;
		};
	}

	private boolean bearish(SetupType type) {

		return switch (type) {

		case BEARISH_BREAKDOWN, BEARISH_PULLBACK, VWAP_BEARISH_REJECTION, BEARISH_REVERSAL -> true;

		default -> false;
		};
	}

	private record SetupSignals(boolean openingRangeBreakout, boolean fakeBreakout, boolean volumeBreakout,
			boolean cprBreakout, boolean multiTimeFrameConfirmed) {
	}
}