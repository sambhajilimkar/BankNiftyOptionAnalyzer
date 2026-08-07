package com.banknifty.recommendation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banknifty.analysis.MarketBias;
import com.banknifty.enums.OptionType;
import com.banknifty.indicator.result.IndicatorSnapshot;

@Service
public class TechnicalAnalysisService {

	public Signal analyse(IndicatorSnapshot indicators) {

		int bullish = 0;
		int bearish = 0;

		List<String> bullishReasons = new ArrayList<>();
		List<String> bearishReasons = new ArrayList<>();

		// EMA
		if (indicators.ema().bullishAlignment()) {
			bullish += 30;
			bullishReasons.add("EMA bullish alignment");
		}

		if (indicators.ema().bearishAlignment()) {
			bearish += 30;
			bearishReasons.add("EMA bearish alignment");
		}

		// RSI
		if (indicators.rsi().bullish() && indicators.rsi().rising()) {
			bullish += 15;
			bullishReasons.add("RSI rising");
		}

		if (indicators.rsi().bearish() && indicators.rsi().falling()) {
			bearish += 15;
			bearishReasons.add("RSI falling");
		}

		// MACD
		if (indicators.macd().bullish() || indicators.macd().bullishCross()) {
			bullish += 20;
			bullishReasons.add("MACD bullish");
		}

		if (indicators.macd().bearish() || indicators.macd().bearishCross()) {
			bearish += 20;
			bearishReasons.add("MACD bearish");
		}

		// VWAP
		if (indicators.vwap().aboveVWAP()) {
			bullish += 25;
			bullishReasons.add("Price above VWAP");
		} else {
			bearish += 25;
			bearishReasons.add("Price below VWAP");
		}

		// ADX
		if (indicators.adx().strongTrend()) {

			if (indicators.adx().bullish()) {
				bullish += 10;
				bullishReasons.add("ADX confirms bullish trend");
			}

			if (indicators.adx().bearish()) {
				bearish += 10;
				bearishReasons.add("ADX confirms bearish trend");
			}
		}

		if (bullish >= bearish) {
			return new Signal(OptionType.CE, Math.min(bullish, 95), bullishReasons);
		}

		return new Signal(OptionType.PE, Math.min(bearish, 95), bearishReasons);
	}

	public MarketBias marketBias(OptionType optionType) {

		return optionType == OptionType.CE ? MarketBias.BULLISH : MarketBias.BEARISH;
	}

	public record Signal(OptionType optionType, int confidence, List<String> reasons) {
	}
}