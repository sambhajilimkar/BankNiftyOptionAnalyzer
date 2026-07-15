package com.banknifty.provider;

import com.banknifty.model.Candle;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction for fetching market data.
 *
 * The analysis engine depends on this interface instead of
 * directly depending on Zerodha Kite SDK.
 */
public interface MarketDataProvider {

    /**
     * Returns latest candles.
     *
     * Example:
     * BANKNIFTY
     * Interval : 5minute
     * Count : 300
     */
    List<Candle> getRecentCandles(
            String tradingSymbol,
            String interval,
            int candleCount
    );

    /**
     * Returns historical candles between dates.
     */
    List<Candle> getHistoricalCandles(
            String tradingSymbol,
            String interval,
            LocalDate fromDate,
            LocalDate toDate
    );

    /**
     * Returns latest traded price.
     */
    double getLastTradedPrice(String tradingSymbol);

    /**
     * Market open?
     */
    boolean isMarketOpen();

    /**
     * Returns today's volume.
     */
    long getTodayVolume(String tradingSymbol);

}