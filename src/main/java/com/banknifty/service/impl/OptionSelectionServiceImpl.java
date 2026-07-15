package com.banknifty.service.impl;

import com.banknifty.config.TradingProperties;
import com.banknifty.enums.SignalType;
import com.banknifty.model.OptionContract;
import com.banknifty.model.TrendScore;
import com.banknifty.service.OptionSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionSelectionServiceImpl
        implements OptionSelectionService {

    private final TradingProperties properties;

    @Override
    public OptionContract selectBestOption(
            TrendScore trendScore,
            double bankNiftySpot,
            List<OptionContract> contracts) {

        SignalType signal = trendScore.buyCE()
                ? SignalType.BUY_CE
                : SignalType.BUY_PE;

        int atmStrike = calculateATM(
                bankNiftySpot,
                properties.getStrikeStep());

        return contracts.stream()

                // CE / PE filter
                .filter(c ->
                        signal == SignalType.BUY_CE
                                ? "CE".equalsIgnoreCase(c.optionType())
                                : "PE".equalsIgnoreCase(c.optionType()))

                // Weekly expiry only (nearest expiry assumed)
                .filter(c ->
                        Math.abs(c.strike() - atmStrike)
                                <= properties.getMaxStrikeDistance())

                // Liquidity
                .filter(c ->
                        c.volume() >= properties.getMinimumVolume())

                .filter(c ->
                        c.openInterest()
                                >= properties.getMinimumOpenInterest())

                // Premium
                .filter(c ->
                        c.ltp().doubleValue()
                                >= properties.getMinimumPremium())

                .filter(c ->
                        c.ltp().doubleValue()
                                <= properties.getMaximumPremium())

                // Spread
                .filter(c ->
                        c.ask().subtract(c.bid()).doubleValue()
                                <= properties.getMaximumSpread())

                // Rank by liquidity
                .max(Comparator
                        .comparing(OptionContract::openInterest)
                        .thenComparing(OptionContract::volume))

                .orElseThrow(() ->
                        new IllegalStateException(
                                "No suitable option found."));
    }

    private int calculateATM(
            double spot,
            int step) {

        return (int)
                (Math.round(spot / step) * step);

    }

}