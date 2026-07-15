package com.banknifty.indicator;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

@Component
public class EMASeriesCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;

    public List<BigDecimal> calculate(
            List<Candle> candles,
            int period) {

        if (candles == null || candles.size() < period) {
            throw new IllegalArgumentException(
                    "Minimum " + period + " candles required.");
        }

        List<BigDecimal> emaSeries = new ArrayList<>();

        BigDecimal multiplier =
                BigDecimal.valueOf(2.0 / (period + 1));

        BigDecimal ema = candles.get(0).close();

        emaSeries.add(ema);

        for (int i = 1; i < candles.size(); i++) {

            BigDecimal close = candles.get(i).close();

            ema = close.subtract(ema)
                    .multiply(multiplier, MC)
                    .add(ema);

            emaSeries.add(ema);
        }

        return emaSeries;
    }

}