package com.banknifty.service.impl;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.RecommendationAction;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.model.Candle;
import com.banknifty.model.Recommendation;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.provider.KiteHistoricalDataProvider;
import com.banknifty.provider.KiteInstrumentProvider;
import com.banknifty.provider.KiteQuoteProvider;
import com.banknifty.recommendation.model.RecommendationRequest;
import com.banknifty.service.OptionRecommendationService;
import com.banknifty.service.TrendAnalysisResult;
import com.banknifty.service.TrendAnalysisService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionRecommendationServiceImpl implements OptionRecommendationService {

    private static final String EXCHANGE = "NSE";
    private static final String INTERVAL = "5minute";
    private static final int HISTORY_DAYS = 7;
    private static final int MINIMUM_TRADE_CONFIDENCE = 60;
    private static final BigDecimal STOP_LOSS_PERCENT = BigDecimal.valueOf(0.20);
    private static final BigDecimal TARGET_1_PERCENT = BigDecimal.valueOf(0.25);
    private static final BigDecimal TARGET_2_PERCENT = BigDecimal.valueOf(0.40);

    private final TrendAnalysisService trendAnalysisService;
    private final KiteHistoricalDataProvider historicalDataProvider;
    private final KiteInstrumentProvider instrumentProvider;
    private final KiteQuoteProvider quoteProvider;
    private final OptionUniverseService optionUniverseService;
    private final TradingProperties tradingProperties;

    @Override
    public Recommendation recommend(String underlying, ExpiryType expiryType) {
        String spotSymbol = spotSymbol(underlying);
        String optionUnderlying = optionUnderlying(underlying);
        BigDecimal liveSpotPrice = liveIndexPrice(spotSymbol);
        TrendAnalysisResult trend = trendAnalysisService.analyze(historicalCandles(spotSymbol));

        if (!isTradeAllowed(trend)) {
            return noTrade(trend, liveSpotPrice);
        }

        OptionQuote contract = selectBestContract(optionUnderlying, expiryType, trend.optionType(), liveSpotPrice);
        BigDecimal entry = contract.ltp();
        List<String> reasons = new ArrayList<>(trend.reasons());
        reasons.add("Selected live " + contract.optionType() + " contract: " + contract.tradingSymbol());
        reasons.add("Contract score " + contractScore(contract, trend.optionType(), liveSpotPrice)
                + " based on premium, strike placement, and distance from spot");

        return Recommendation.builder()
                .action(RecommendationAction.BUY)
                .optionType(contract.optionType())
                .tradingSymbol(contract.tradingSymbol())
                .strike(contract.strike())
                .spotPrice(liveSpotPrice)
                .entry(entry)
                .stopLoss(level(entry, BigDecimal.ONE.subtract(STOP_LOSS_PERCENT)))
                .target1(level(entry, BigDecimal.ONE.add(TARGET_1_PERCENT)))
                .target2(level(entry, BigDecimal.ONE.add(TARGET_2_PERCENT)))
                .confidence(trend.confidence())
                .reasons(List.copyOf(reasons))
                .build();
    }

    private boolean isTradeAllowed(TrendAnalysisResult trend) {
        return trend.confidence() >= MINIMUM_TRADE_CONFIDENCE
                && trend.action() != RecommendationAction.SIDEWAYS
                && trend.action() != RecommendationAction.HOLD
                && trend.action() != RecommendationAction.WAIT;
    }

    private Recommendation noTrade(TrendAnalysisResult trend, BigDecimal spotPrice) {
        List<String> reasons = new ArrayList<>(trend.reasons());
        reasons.add("No trade: directional confidence must be at least " + MINIMUM_TRADE_CONFIDENCE);

        return Recommendation.builder()
                .action(RecommendationAction.SIDEWAYS)
                .optionType(trend.optionType())
                .tradingSymbol(null)
                .strike(null)
                .spotPrice(spotPrice)
                .entry(BigDecimal.ZERO)
                .stopLoss(BigDecimal.ZERO)
                .target1(BigDecimal.ZERO)
                .target2(BigDecimal.ZERO)
                .confidence(trend.confidence())
                .reasons(List.copyOf(reasons))
                .build();
    }

    private OptionQuote selectBestContract(
            String underlying,
            ExpiryType expiryType,
            OptionType optionType,
            BigDecimal spotPrice) {

        RecommendationRequest request = RecommendationRequest.builder()
                .instrument(underlying)
                .expiryType(expiryType)
                .tradingStyle(TradingStyle.INTRADAY)
                .riskProfile(RiskProfile.BALANCED)
                .capital(null)
                .build();

        return optionUniverseService.loadUniverse(request).stream()
                .filter(quote -> quote.optionType() == optionType)
                .filter(this::hasTradablePremium)
                .filter(quote -> strikeDistance(quote, spotPrice)
                        .compareTo(BigDecimal.valueOf(tradingProperties.getMaxStrikeDistance())) <= 0)
                .max(Comparator.comparingInt(quote -> contractScore(quote, optionType, spotPrice)))
                .orElseThrow(() -> new IllegalStateException(
                        "No liquid " + optionType + " contract matched the configured trading rules"));
    }

    private boolean hasTradablePremium(OptionQuote quote) {
        if (quote.ltp() == null || quote.ltp().signum() <= 0 || quote.strike() == null) {
            return false;
        }
        return quote.ltp().compareTo(BigDecimal.valueOf(tradingProperties.getMinimumPremium())) >= 0
                && quote.ltp().compareTo(BigDecimal.valueOf(tradingProperties.getMaximumPremium())) <= 0;
    }

    private int contractScore(OptionQuote quote, OptionType optionType, BigDecimal spotPrice) {
        BigDecimal distance = strikeDistance(quote, spotPrice);
        int score = 0;

        if (isSlightlyInTheMoney(quote, optionType, spotPrice)) {
            score += 40;
        } else if (distance.compareTo(BigDecimal.ZERO) == 0) {
            score += 35;
        } else {
            score += 20;
        }

        if (distance.compareTo(BigDecimal.valueOf(100)) <= 0) {
            score += 30;
        } else if (distance.compareTo(BigDecimal.valueOf(200)) <= 0) {
            score += 15;
        }

        BigDecimal preferredPremium = BigDecimal.valueOf(
                (tradingProperties.getMinimumPremium() + tradingProperties.getMaximumPremium()) / 2);
        BigDecimal premiumDistance = quote.ltp().subtract(preferredPremium).abs();
        if (premiumDistance.compareTo(BigDecimal.valueOf(75)) <= 0) {
            score += 20;
        } else if (premiumDistance.compareTo(BigDecimal.valueOf(150)) <= 0) {
            score += 10;
        }

        return score;
    }

    private boolean isSlightlyInTheMoney(OptionQuote quote, OptionType optionType, BigDecimal spotPrice) {
        BigDecimal strike = BigDecimal.valueOf(quote.strike());
        BigDecimal distance = strike.subtract(spotPrice).abs();
        return distance.compareTo(BigDecimal.valueOf(tradingProperties.getStrikeStep())) <= 0
                && ((optionType == OptionType.CE && strike.compareTo(spotPrice) < 0)
                        || (optionType == OptionType.PE && strike.compareTo(spotPrice) > 0));
    }

    private BigDecimal strikeDistance(OptionQuote quote, BigDecimal spotPrice) {
        return BigDecimal.valueOf(quote.strike()).subtract(spotPrice).abs();
    }

    private List<Candle> historicalCandles(String symbol) {
        LocalDateTime to = LocalDateTime.now();
        try {
            return historicalDataProvider.fetchHistoricalData(
                    instrumentProvider.getInstrumentToken(EXCHANGE, symbol),
                    symbol,
                    EXCHANGE,
                    INTERVAL,
                    to.minusDays(HISTORY_DAYS),
                    to,
                    false,
                    false);
        } catch (Exception | KiteException exception) {
            throw new IllegalStateException("Unable to load Zerodha historical candles for " + symbol, exception);
        }
    }

    private BigDecimal liveIndexPrice(String symbol) {
        try {
            BigDecimal ltp = quoteProvider.getLTP(EXCHANGE + ":" + symbol);
            if (ltp == null || ltp.signum() <= 0) {
                throw new IllegalStateException("Zerodha returned no live index price for " + symbol);
            }
            return ltp;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load Zerodha live index price for " + symbol, exception);
        }
    }

    private String spotSymbol(String underlying) {
        if (underlying == null || underlying.isBlank()) {
            return "NIFTY BANK";
        }
        String symbol = underlying.trim().toUpperCase();
        return symbol.equals("BANKNIFTY") || symbol.equals("BANK NIFTY") ? "NIFTY BANK" : symbol;
    }

    private String optionUnderlying(String underlying) {
        if (underlying == null || underlying.isBlank()) {
            return "BANKNIFTY";
        }
        String symbol = underlying.trim().toUpperCase();
        return symbol.equals("NIFTY BANK") || symbol.equals("BANK NIFTY") ? "BANKNIFTY" : symbol;
    }

    private BigDecimal level(BigDecimal entry, BigDecimal multiplier) {
        return entry.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
