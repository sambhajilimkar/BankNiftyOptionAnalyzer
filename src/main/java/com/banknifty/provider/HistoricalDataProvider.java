package com.banknifty.provider;

import com.banknifty.model.Candle;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provider abstraction for fetching historical market data.
 */
public interface HistoricalDataProvider {

    /**
     * Fetch historical candles from market data provider.
     *
     * @param instrumentToken Zerodha instrument token
     * @param tradingSymbol Trading symbol
     * @param exchange Exchange (NSE/NFO)
     * @param interval Candle interval
     * @param from Start time
     * @param to End time
     * @param continuous Continuous futures flag
     * @param openInterest Include OI
     * @return Historical candles
     */
    List<Candle> fetchHistoricalData(
            Long instrumentToken,
            String tradingSymbol,
            String exchange,
            String interval,
            LocalDateTime from,
            LocalDateTime to,
            boolean continuous,
            boolean openInterest);

}