package com.banknifty.analysis;

import com.banknifty.indicator.ADXIndicatorEngine;
import com.banknifty.indicator.ATRIndicatorEngine;
import com.banknifty.indicator.EMAIndicatorEngine;
import com.banknifty.indicator.MACDIndicatorEngine;
import com.banknifty.indicator.RSIIndicatorEngine;
import com.banknifty.indicator.VWAPIndicatorEngine;
import com.banknifty.indicator.result.IndicatorSnapshot;
import com.banknifty.market.realtime.CandleCache;
import com.banknifty.model.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IndicatorPipeline {

	private static final int MINIMUM_CANDLES = 200;

	private final CandleCache candleCache;

	private final EMAIndicatorEngine emaIndicatorEngine;

	private final RSIIndicatorEngine rsiIndicatorEngine;

	private final MACDIndicatorEngine macdIndicatorEngine;

	private final VWAPIndicatorEngine vwapIndicatorEngine;

	private final ADXIndicatorEngine adxIndicatorEngine;

	private final ATRIndicatorEngine atrIndicatorEngine;

	// Ready for next module
	// private final SuperTrendIndicatorEngine superTrendIndicatorEngine;

	public IndicatorPipeline(CandleCache candleCache, EMAIndicatorEngine emaIndicatorEngine,
			RSIIndicatorEngine rsiIndicatorEngine, MACDIndicatorEngine macdIndicatorEngine,
			VWAPIndicatorEngine vwapIndicatorEngine, ADXIndicatorEngine adxIndicatorEngine,
			ATRIndicatorEngine atrIndicatorEngine) {

		this.candleCache = candleCache;
		this.emaIndicatorEngine = emaIndicatorEngine;
		this.rsiIndicatorEngine = rsiIndicatorEngine;
		this.macdIndicatorEngine = macdIndicatorEngine;
		this.vwapIndicatorEngine = vwapIndicatorEngine;
		this.adxIndicatorEngine = adxIndicatorEngine;
		this.atrIndicatorEngine = atrIndicatorEngine;
	}

	/**
	 * Calculate indicators from Candle Cache.
	 */
	public IndicatorSnapshot calculate(Long instrumentToken) {

		List<Candle> candles = candleCache.get(instrumentToken);

		return calculate(candles);

	}

	/**
	 * Stateless calculation. Can be used by: - Live Trading - Backtesting -
	 * Historical Analysis - Unit Testing
	 */
	public IndicatorSnapshot calculate(List<Candle> candles) {

		validate(candles);

		log.debug("Calculating indicators using {} candles.", candles.size());

		return IndicatorSnapshot.builder()

				.ema(emaIndicatorEngine.calculate(candles))

				.rsi(rsiIndicatorEngine.calculate(candles))

				.macd(macdIndicatorEngine.calculate(candles))

				.vwap(vwapIndicatorEngine.calculate(candles))

				.adx(adxIndicatorEngine.calculate(candles))

				.atr(atrIndicatorEngine.calculate(candles))

				// Uncomment after implementation
				// .superTrend(superTrendIndicatorEngine.calculate(candles))

				.build();

	}

	/**
	 * Validate minimum candles.
	 */
	private void validate(List<Candle> candles) {

		if (candles == null || candles.isEmpty()) {

			throw new IllegalArgumentException("Historical candles cannot be null.");

		}

		if (candles.size() < MINIMUM_CANDLES) {

			throw new IllegalArgumentException("Minimum " + MINIMUM_CANDLES + " candles required.");

		}

	}

}