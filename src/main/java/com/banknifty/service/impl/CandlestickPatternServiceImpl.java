package com.banknifty.service.impl;

import com.banknifty.enums.CandlestickPattern;
import com.banknifty.model.Candle;
import com.banknifty.model.PatternResult;
import com.banknifty.service.CandlestickPatternService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandlestickPatternServiceImpl
        implements CandlestickPatternService {

    @Override
    public PatternResult analyze(List<Candle> candles) {

        if (candles.size() < 2) {

            return PatternResult.builder()
                    .pattern(CandlestickPattern.NONE)
                    .score(0)
                    .build();

        }

        Candle previous =
                candles.get(candles.size() - 2);

        Candle current =
                candles.get(candles.size() - 1);

        boolean previousBearish =
                previous.close().compareTo(previous.open()) < 0;

        boolean currentBullish =
                current.close().compareTo(current.open()) > 0;

        boolean bullishEngulfing =
                previousBearish &&
                currentBullish &&
                current.open().compareTo(previous.close()) <= 0 &&
                current.close().compareTo(previous.open()) >= 0;

        if (bullishEngulfing) {

            return PatternResult.builder()

                    .pattern(CandlestickPattern.BULLISH_ENGULFING)

                    .bullish(true)

                    .bearish(false)

                    .score(20)

                    .description("Bullish Engulfing")

                    .build();

        }

        return PatternResult.builder()

                .pattern(CandlestickPattern.NONE)

                .score(0)

                .build();

    }

}