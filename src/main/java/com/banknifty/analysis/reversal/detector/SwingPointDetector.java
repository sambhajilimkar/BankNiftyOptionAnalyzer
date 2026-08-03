package com.banknifty.analysis.reversal.detector;

import com.banknifty.analysis.reversal.model.SwingPoint;
import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SwingPointDetector {

    private static final int WINDOW = 2;

    public List<SwingPoint> highs(List<Candle> candles, List<Double> indicator) {

        List<SwingPoint> result = new ArrayList<>();

        for (int i = WINDOW; i < candles.size() - WINDOW; i++) {

            boolean highest = true;

            for (int j = i - WINDOW; j <= i + WINDOW; j++) {

                if (j == i) {
                    continue;
                }

                if (candles.get(j).high().doubleValue()
                        >= candles.get(i).high().doubleValue()) {

                    highest = false;
                    break;
                }
            }

            if (highest) {

                result.add(
                        SwingPoint.builder()
                                .index(i)
                                .price(candles.get(i).high().doubleValue())
                                .indicator(indicator.get(i))
                                .build());
            }
        }

        return result;
    }

    public List<SwingPoint> lows(List<Candle> candles, List<Double> indicator) {

        List<SwingPoint> result = new ArrayList<>();

        for (int i = WINDOW; i < candles.size() - WINDOW; i++) {

            boolean lowest = true;

            for (int j = i - WINDOW; j <= i + WINDOW; j++) {

                if (j == i) {
                    continue;
                }

                if (candles.get(j).low().doubleValue()
                        <= candles.get(i).low().doubleValue()) {

                    lowest = false;
                    break;
                }
            }

            if (lowest) {

                result.add(
                        SwingPoint.builder()
                                .index(i)
                                .price(candles.get(i).low().doubleValue())
                                .indicator(indicator.get(i))
                                .build());
            }
        }

        return result;
    }
}