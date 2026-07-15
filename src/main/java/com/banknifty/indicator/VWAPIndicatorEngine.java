package com.banknifty.indicator;

import com.banknifty.indicator.result.VWAPResult;
import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Component
public class VWAPIndicatorEngine extends AbstractIndicator<VWAPResult> {

	private static final MathContext MC = MathContext.DECIMAL64;

	public VWAPIndicatorEngine(BarSeriesBuilder barSeriesBuilder) {
		super(barSeriesBuilder);
	}

	@Override
	public VWAPResult calculate(List<Candle> candles) {

		validate(candles, 2);

		BigDecimal cumulativePV = BigDecimal.ZERO;
		BigDecimal cumulativeVolume = BigDecimal.ZERO;

		for (Candle candle : candles) {

			BigDecimal typicalPrice = candle.high().add(candle.low()).add(candle.close()).divide(BigDecimal.valueOf(3),
					8, RoundingMode.HALF_UP);

			BigDecimal volume = BigDecimal.valueOf(candle.volume());

			cumulativePV = cumulativePV.add(typicalPrice.multiply(volume, MC));

			cumulativeVolume = cumulativeVolume.add(volume);

		}

		BigDecimal vwap;

		if (cumulativeVolume.compareTo(BigDecimal.ZERO) == 0) {

			vwap = BigDecimal.ZERO;

		} else {

			vwap = cumulativePV.divide(cumulativeVolume, 8, RoundingMode.HALF_UP);

		}

		BigDecimal lastPrice = candles.get(candles.size() - 1).close();

		boolean aboveVWAP = lastPrice.compareTo(vwap) > 0;

		BigDecimal distance = lastPrice.subtract(vwap);

		boolean breakout = distance.compareTo(BigDecimal.ZERO) > 0;

		boolean pullback = distance.compareTo(BigDecimal.ZERO) < 0;

		return new VWAPResult(

				vwap,

				lastPrice,

				distance,

				aboveVWAP,

				breakout,

				pullback

		);

	}

	/**
	 * Convenience method.
	 */
	public boolean isPriceAboveVWAP(List<Candle> candles) {

		return calculate(candles).priceAboveVWAP();

	}

	public boolean isPriceBelowVWAP(List<Candle> candles) {

		return !calculate(candles).priceAboveVWAP();

	}

}