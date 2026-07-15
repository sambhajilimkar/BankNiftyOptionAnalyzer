package com.banknifty.service;

import com.banknifty.entity.InstrumentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InstrumentCache {

    /**
     * Key format:
     *
     * NSE:NIFTY BANK
     * NFO:BANKNIFTY25JUL58000CE
     */
    private final Map<String, InstrumentEntity> cache =
            new ConcurrentHashMap<>();

    public void put(InstrumentEntity instrument) {

        String key = buildKey(
                instrument.getExchange(),
                instrument.getTradingSymbol());

        cache.put(key, instrument);
    }

    public void putAll(Collection<InstrumentEntity> instruments) {

        instruments.forEach(this::put);

        log.info("Loaded {} instruments into cache.", cache.size());
    }

    public Optional<InstrumentEntity> find(
            String exchange,
            String tradingSymbol) {

        return Optional.ofNullable(
                cache.get(buildKey(exchange, tradingSymbol))
        );
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    private String buildKey(
            String exchange,
            String tradingSymbol) {

        return exchange.toUpperCase() +
                ":" +
                tradingSymbol.toUpperCase();
    }

}