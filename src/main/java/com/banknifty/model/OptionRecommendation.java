package com.banknifty.model;

import com.banknifty.enums.SignalType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Final option recommendation produced by Recommendation Engine.
 */
@Builder(toBuilder = true)
public record OptionRecommendation(

        SignalType signal,

        String tradingSymbol,

        Long instrumentToken,

        String optionType,

        Integer strike,

        BigDecimal entryPrice,

        BigDecimal stopLoss,

        BigDecimal target,

        Integer confidence,

        String reason,

        LocalDateTime generatedAt

) {
}