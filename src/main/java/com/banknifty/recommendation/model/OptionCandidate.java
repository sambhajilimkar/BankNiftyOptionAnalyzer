package com.banknifty.recommendation.model;

import com.banknifty.analysis.MarketBias;
import com.banknifty.enums.OptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionCandidate {

	/*
	 * ============================================================ Broker
	 * Information ============================================================
	 */

	private Long instrumentToken;

	private String tradingSymbol;

	private Integer strike;

	private LocalDate expiry;

	private OptionType optionType;

	private BigDecimal premium;

	/*
	 * ============================================================ Market
	 * Information ============================================================
	 */

	private BigDecimal spotPrice;

	private BigDecimal distanceFromATM;

	private BigDecimal distancePercent;

	private MarketBias marketBias;

	/*
	 * ============================================================ Liquidity
	 * ============================================================
	 */

	private Long volume;

	private Long openInterest;

	private BigDecimal bid;

	private BigDecimal ask;

	private BigDecimal spread;

	private BigDecimal spreadPercentage;

	private BigDecimal liquidityIndex;

	/*
	 * ============================================================ Greeks
	 * ============================================================
	 */

	private BigDecimal iv;

	private BigDecimal delta;

	private BigDecimal theta;

	private BigDecimal gamma;

	private BigDecimal vega;

	/*
	 * ============================================================ Price Structure
	 * ============================================================
	 */

	private BigDecimal support;

	private BigDecimal resistance;

	private BigDecimal pivot;

	/*
	 * ============================================================ Strike
	 * Classification ============================================================
	 */

	private boolean atm;

	private boolean itm;

	private boolean otm;

	private Integer strikeDistance;

	/*
	 * ============================================================ Option Analytics
	 * ============================================================
	 */

	private BigDecimal intrinsicValue;

	private BigDecimal timeValue;

	private BigDecimal expectedMove;

	private BigDecimal probabilityIndex;

	/*
	 * ============================================================ Calculated Score
	 * ============================================================
	 */

	private OptionScore score;

}