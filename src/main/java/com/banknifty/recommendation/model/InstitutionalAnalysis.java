package com.banknifty.recommendation.model;

import java.math.BigDecimal;

import com.banknifty.analysis.MarketBias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalAnalysis {

	/*
	 * ============================================================ Overall Market
	 * ============================================================
	 */

	private MarketBias marketBias;

	private double confidence;

	/*
	 * ============================================================ PCR
	 * ============================================================
	 */

	private BigDecimal putCallRatio;

	private double pcrScore;

	/*
	 * ============================================================ Open Interest
	 * ============================================================
	 */

	private long totalCallOI;

	private long totalPutOI;

	private long totalCallVolume;

	private long totalPutVolume;

	private double oiScore;

	/*
	 * ============================================================ OI Build-up
	 * ============================================================
	 */

	private double longBuildUpScore;

	private double shortBuildUpScore;

	private double longUnwindingScore;

	private double shortCoveringScore;

	/*
	 * ============================================================ Institutional
	 * Writing ============================================================
	 */

	private double callWritingStrength;

	private double putWritingStrength;

	/*
	 * ============================================================ Max Pain
	 * ============================================================
	 */

	private Integer maxPainStrike;

	private double maxPainScore;

	/*
	 * ============================================================ Support /
	 * Resistance ============================================================
	 */

	private Integer strongestSupportStrike;

	private Integer strongestResistanceStrike;

	private double supportResistanceScore;

	/*
	 * ============================================================ Greeks
	 * ============================================================
	 */

	private BigDecimal totalDelta;

	private BigDecimal totalGamma;

	private BigDecimal totalTheta;

	private BigDecimal totalVega;

	private double gammaExposureScore;

	/*
	 * ============================================================ Volatility
	 * ============================================================
	 */

	private BigDecimal averageIV;

	private BigDecimal ivPercentile;

	private double volatilityScore;

	/*
	 * ============================================================ Liquidity
	 * ============================================================
	 */

	private BigDecimal averageSpread;

	private BigDecimal averageLiquidity;

	private double liquidityScore;

	/*
	 * ============================================================ Institutional
	 * Composite Score ============================================================
	 */

	private double institutionalScore;

}