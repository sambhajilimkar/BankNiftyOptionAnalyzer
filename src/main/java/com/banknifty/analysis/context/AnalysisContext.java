package com.banknifty.analysis.context;

import com.banknifty.analysis.MarketBias;
import com.banknifty.indicator.result.ADXResult;
import com.banknifty.indicator.result.EMAResult;
import com.banknifty.indicator.result.MACDResult;
import com.banknifty.indicator.result.RSIResult;
import com.banknifty.indicator.result.VWAPResult;
import com.banknifty.service.OpenInterestAnalysisService;
import com.banknifty.service.PivotResult;
import com.banknifty.service.SupportResistanceResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisContext {

	/*
	 * ============================================================ Market
	 * Information ============================================================
	 */

	private BigDecimal spotPrice;

	private MarketBias marketBias;

	/*
	 * ============================================================ Overall Trend
	 * ============================================================
	 */

	private Integer trendScore;

	private Integer confidence;

	/*
	 * ============================================================ Technical
	 * Indicators ============================================================
	 */

	private EMAResult ema;

	private RSIResult rsi;

	private MACDResult macd;

	private VWAPResult vwap;

	private ADXResult adx;

	/*
	 * ============================================================ Open Interest
	 * Analysis ============================================================
	 */

	private OpenInterestAnalysisService.OpenInterestResult openInterest;

	/*
	 * ============================================================ Market Structure
	 * ============================================================
	 */

	private SupportResistanceResult supportResistance;

	private PivotResult pivot;

}