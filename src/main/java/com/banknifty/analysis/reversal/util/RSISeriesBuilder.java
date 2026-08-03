package com.banknifty.analysis.reversal.util;

import com.banknifty.model.Candle;
import com.banknifty.util.BarSeriesBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RSISeriesBuilder {

    private final BarSeriesBuilder barSeriesBuilder;

    public List<Double> build(List<Candle> candles) {

        BarSeries series = barSeriesBuilder.build(candles);

        ClosePriceIndicator closePrice =
                new ClosePriceIndicator(series);

        RSIIndicator rsi =
                new RSIIndicator(closePrice, 14);

        List<Double> values = new ArrayList<>(series.getBarCount());

        for (int i = 0; i < series.getBarCount(); i++) {
            values.add(rsi.getValue(i).doubleValue());
        }

        return values;
    }

}