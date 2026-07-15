package com.banknifty.service;

import com.banknifty.model.Candle;
import com.banknifty.model.PatternResult;

import java.util.List;

public interface CandlestickPatternService {

    PatternResult analyze(List<Candle> candles);

}