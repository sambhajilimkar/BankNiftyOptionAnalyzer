package com.banknifty.recommendation.engine;

import com.banknifty.analysis.MarketBias;
import com.banknifty.analysis.context.AnalysisContext;
import com.banknifty.enums.OptionType;
import com.banknifty.recommendation.model.InstitutionalAnalysis;
import com.banknifty.recommendation.model.OptionAnalysis;
import com.banknifty.recommendation.model.OptionCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class OptionAnalysisEngine {

	public OptionAnalysis analyze(AnalysisContext context, OptionCandidate candidate) {

		OptionAnalysis analysis = OptionAnalysis.builder().candidate(candidate).build();

		InstitutionalAnalysis institutional = context.getInstitutionalAnalysis();

		scoreTrend(context, candidate, analysis);

		scoreStrike(candidate, analysis);

		scoreLiquidity(candidate, analysis);

		scoreOpenInterest(context, analysis);

		scoreSupportResistance(context, analysis);

		scorePivot(context, analysis);

		scoreExpiry(candidate, analysis);

		scoreVolatility(candidate, analysis);

		scoreGreeks(candidate, analysis);

		scoreInstitutional(institutional, candidate, analysis);

		scoreRiskReward(context, candidate, analysis);

		calculateTradeLevels(candidate, analysis);

		analysis.calculateConfidence();

		return analysis;
	}

	private void scoreInstitutional(InstitutionalAnalysis institutional, OptionCandidate candidate,
			OptionAnalysis analysis) {

		if (institutional == null) {
			return;
		}

		double score = 0;

		/*
		 * ===================================================== Market Bias
		 * =====================================================
		 */

		if (candidate.getOptionType() == OptionType.CE) {

			switch (institutional.getMarketBias()) {

			case STRONG_BULLISH -> {
				score += 12;
				analysis.addReason("Institutional Bullish");
			}

			case BULLISH -> {
				score += 8;
				analysis.addReason("Institutional Bullish");
			}

			default -> {
			}
			}

		} else {

			switch (institutional.getMarketBias()) {

			case STRONG_BEARISH -> {
				score += 12;
				analysis.addReason("Institutional Bearish");
			}

			case BEARISH -> {
				score += 8;
				analysis.addReason("Institutional Bearish");
			}

			default -> {
			}
			}
		}

		/*
		 * ===================================================== PCR
		 * =====================================================
		 */

		BigDecimal pcr = institutional.getPutCallRatio();

		if (pcr != null) {

			if (pcr.compareTo(BigDecimal.valueOf(1.15)) > 0 && candidate.getOptionType() == OptionType.CE) {

				score += 4;
				analysis.addReason("Bullish PCR");

			} else if (pcr.compareTo(BigDecimal.valueOf(0.85)) < 0 && candidate.getOptionType() == OptionType.PE) {

				score += 4;
				analysis.addReason("Bearish PCR");
			}
		}

		/*
		 * ===================================================== Max Pain
		 * =====================================================
		 */

		if (institutional.getMaxPainStrike() != null) {

			int distance = Math.abs(candidate.getStrike() - institutional.getMaxPainStrike());

			if (distance <= 100) {

				score += 3;
				analysis.addReason("Near Max Pain");
			}
		}

		/*
		 * ===================================================== Gamma Exposure
		 * =====================================================
		 */

		score += institutional.getGammaExposureScore() * 0.10;

		/*
		 * ===================================================== OI Score
		 * =====================================================
		 */

		score += institutional.getOiScore() * 0.20;

		analysis.addScore(score);

		analysis.addReason("Institutional Score : " + String.format("%.2f", score));
	}

	private void scoreOpenInterest(AnalysisContext context, OptionAnalysis analysis) {

		double score = 0;

		if (context.getOpenInterest() != null) {

			switch (context.getOpenInterest().trend()) {

			case LONG_BUILDUP -> {
				score = 10;
				analysis.addReason("Long Build-up");
			}

			case SHORT_BUILDUP -> {
				score = 10;
				analysis.addReason("Short Build-up");
			}

			case SHORT_COVERING -> {
				score = 8;
				analysis.addReason("Short Covering");
			}

			case LONG_UNWINDING -> {
				score = 8;
				analysis.addReason("Long Unwinding");
			}

			default -> {
				score = 4;
				analysis.addReason("Neutral OI");
			}
			}
		}

		analysis.setOpenInterestScore(score);
		analysis.addScore(score);
	}

	private void scoreTrend(AnalysisContext context, OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (context.getMarketBias() == MarketBias.STRONG_BULLISH && candidate.getOptionType() == OptionType.CE) {

			score = 25;
			analysis.addReason("Strong Bullish Trend");

		} else if (context.getMarketBias() == MarketBias.BULLISH && candidate.getOptionType() == OptionType.CE) {

			score = 20;
			analysis.addReason("Bullish Trend");

		} else if (context.getMarketBias() == MarketBias.STRONG_BEARISH && candidate.getOptionType() == OptionType.PE) {

			score = 25;
			analysis.addReason("Strong Bearish Trend");

		} else if (context.getMarketBias() == MarketBias.BEARISH && candidate.getOptionType() == OptionType.PE) {

			score = 20;
			analysis.addReason("Bearish Trend");
		}

		analysis.setTrendScore(score);
		analysis.addScore(score);
	}

	private void scoreStrike(OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (candidate.isAtm()) {

			score = 15;
			analysis.addReason("ATM Strike");

		} else if (candidate.isItm()) {

			score = 12;
			analysis.addReason("ITM Strike");

		} else if (candidate.isOtm()) {

			score = 8;
			analysis.addReason("OTM Strike");
		}

		analysis.setStrikeScore(score);
		analysis.addScore(score);
	}

	private void scoreLiquidity(OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (candidate.getLiquidityIndex() != null) {

			score = candidate.getLiquidityIndex().doubleValue();

			if (score > 10) {
				score = 10;
			}
		}

		if (candidate.getSpreadPercentage() != null && candidate.getSpreadPercentage().doubleValue() < 1.0) {

			score += 2;
			analysis.addReason("Tight Bid Ask Spread");
		}

		analysis.setLiquidityScore(score);
		analysis.addScore(score);
	}

	private void scoreSupportResistance(AnalysisContext context, OptionAnalysis analysis) {

		if (context.getSupportResistance() == null) {
			return;
		}

		double score = 0;

		if (context.getSupportResistance().nearSupport()) {

			score += 5;
			analysis.addReason("Near Support");
		}

		if (context.getSupportResistance().breakout()) {

			score += 5;
			analysis.addReason("Breakout");
		}

		if (context.getSupportResistance().nearResistance()) {

			score -= 2;
			analysis.addReason("Near Resistance");
		}

		if (context.getSupportResistance().breakdown()) {

			score -= 3;
			analysis.addReason("Breakdown");
		}

		analysis.setSupportResistanceScore(score);
		analysis.addScore(score);
	}

	private void scorePivot(AnalysisContext context, OptionAnalysis analysis) {

		if (context.getPivot() == null) {
			return;
		}

		double score = 0;

		if (context.getPivot().bullish()) {

			score += 5;
			analysis.addReason("Bullish Pivot");
		}

		if (context.getPivot().bearish()) {

			score += 5;
			analysis.addReason("Bearish Pivot");
		}

		if (context.getPivot().narrowCPR()) {

			score += 3;
			analysis.addReason("Narrow CPR");
		}

		if (context.getPivot().wideCPR()) {

			score -= 2;
			analysis.addReason("Wide CPR");
		}

		analysis.setPivotScore(score);
		analysis.addScore(score);
	}

	private void scoreExpiry(OptionCandidate candidate, OptionAnalysis analysis) {

		if (candidate.getExpiry() == null) {
			return;
		}

		long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), candidate.getExpiry());

		double score;

		if (days <= 1) {

			score = 5;
			analysis.addReason("Expiry Day");

		} else if (days <= 3) {

			score = 4;
			analysis.addReason("Near Expiry");

		} else if (days <= 7) {

			score = 3;

		} else {

			score = 2;
		}

		analysis.setExpiryScore(score);
		analysis.addScore(score);
	}

	private void scoreVolatility(OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (candidate.getIv() != null) {

			double iv = candidate.getIv().doubleValue();

			if (iv >= 10 && iv <= 25) {

				score = 5;
				analysis.addReason("Healthy IV");

			} else if (iv > 25 && iv <= 35) {

				score = 4;
				analysis.addReason("Moderate IV");

			} else if (iv > 35) {

				score = 2;
				analysis.addReason("High IV");

			} else {

				score = 3;
			}
		}

		analysis.setVolatilityScore(score);
		analysis.addScore(score);
	}

	private void scoreGreeks(OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (candidate.getDelta() != null) {

			double delta = Math.abs(candidate.getDelta().doubleValue());

			if (delta >= 0.45 && delta <= 0.70) {

				score += 2;
				analysis.addReason("Good Delta");
			}
		}

		if (candidate.getTheta() != null) {

			if (candidate.getTheta().doubleValue() > -5) {

				score += 1;
				analysis.addReason("Low Theta Decay");
			}
		}

		if (candidate.getGamma() != null) {

			if (candidate.getGamma().doubleValue() > 0) {
				score += 1;
			}
		}

		if (candidate.getVega() != null) {

			if (candidate.getVega().doubleValue() > 0) {
				score += 1;
			}
		}

		score = Math.min(score, 5);

		analysis.setGreekScore(score);
		analysis.addScore(score);
	}

	private void scoreRiskReward(AnalysisContext context, OptionCandidate candidate, OptionAnalysis analysis) {

		double score = 0;

		if (context.getSupportResistance() != null) {

			if (candidate.getOptionType() == OptionType.CE && context.getSupportResistance().breakout()) {

				score += 5;
				analysis.addReason("Bullish Breakout");
			}

			if (candidate.getOptionType() == OptionType.PE && context.getSupportResistance().breakdown()) {

				score += 5;
				analysis.addReason("Bearish Breakdown");
			}
		}

		analysis.setRiskRewardScore(score);
		analysis.addScore(score);
	}

	private void calculateTradeLevels(OptionCandidate candidate, OptionAnalysis analysis) {

		BigDecimal premium = candidate.getPremium();

		if (premium == null) {
			return;
		}

		analysis.setEntry(premium);

		analysis.setStopLoss(premium.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP));

		analysis.setTarget1(premium.multiply(BigDecimal.valueOf(1.20)).setScale(2, RoundingMode.HALF_UP));

		analysis.setTarget2(premium.multiply(BigDecimal.valueOf(1.40)).setScale(2, RoundingMode.HALF_UP));
	}

}
