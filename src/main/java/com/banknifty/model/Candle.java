package com.banknifty.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Candle(

        Long instrumentToken,

        String tradingSymbol,

        String exchange,

        String interval,

        LocalDate tradeDate,

        LocalDateTime dateTime,

        BigDecimal open,

        BigDecimal high,

        BigDecimal low,

        BigDecimal close,

        Long volume,

        Long openInterest,

        BigDecimal averagePrice,

        BigDecimal vwap,

        Integer tickCount,

        boolean completed

) {

    public boolean bullish() {
        return close.compareTo(open) > 0;
    }

    public boolean bearish() {
        return close.compareTo(open) < 0;
    }

    public BigDecimal body() {
        return close.subtract(open).abs();
    }

    public BigDecimal range() {
        return high.subtract(low);
    }

    public BigDecimal midpoint() {
        return high.add(low)
                .divide(BigDecimal.valueOf(2));
    }

    public boolean doji() {

        if (range().doubleValue() == 0)
            return false;

        return body().doubleValue() / range().doubleValue() < 0.10;
    }

}