package com.banknifty.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record OptionContract(

        String tradingSymbol,

        Long instrumentToken,

        LocalDate expiry,

        Integer strike,

        String optionType,

        BigDecimal ltp,

        Long volume,

        Long openInterest,

        BigDecimal bid,

        BigDecimal ask,

        BigDecimal iv

) {
}