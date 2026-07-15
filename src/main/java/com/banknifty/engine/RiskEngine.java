package com.banknifty.engine;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Risk management calculations for trade recommendations.
 */
@Component
public class RiskEngine {

    private static final BigDecimal DEFAULT_SL_PERCENT = BigDecimal.valueOf(0.02);
    private static final BigDecimal DEFAULT_TARGET1_PERCENT = BigDecimal.valueOf(0.03);
    private static final BigDecimal DEFAULT_TARGET2_PERCENT = BigDecimal.valueOf(0.06);

    public BigDecimal entryPrice(List<Candle> candles) {
        validate(candles);
        return candles.get(candles.size() - 1).close();
    }

    public BigDecimal stopLoss(BigDecimal entry) {
        return entry.multiply(BigDecimal.ONE.subtract(DEFAULT_SL_PERCENT))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal target1(BigDecimal entry) {
        return entry.multiply(BigDecimal.ONE.add(DEFAULT_TARGET1_PERCENT))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal target2(BigDecimal entry) {
        return entry.multiply(BigDecimal.ONE.add(DEFAULT_TARGET2_PERCENT))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal riskReward(BigDecimal entry,
                                 BigDecimal stopLoss,
                                 BigDecimal target) {

        BigDecimal risk = entry.subtract(stopLoss).abs();
        BigDecimal reward = target.subtract(entry).abs();

        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return reward.divide(risk, 2, RoundingMode.HALF_UP);
    }

    private void validate(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            throw new IllegalArgumentException("Candles cannot be empty");
        }
    }
}
