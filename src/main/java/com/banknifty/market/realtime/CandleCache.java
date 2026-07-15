package com.banknifty.market.realtime;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleCache {

	private final Map<Long, List<Candle>> candleMap = new ConcurrentHashMap<>();

	public void add(Long instrumentToken, Candle candle) {

		candleMap.computeIfAbsent(instrumentToken, k -> new ArrayList<>()).add(candle);

	}

	public List<Candle> get(Long instrumentToken) {

		return candleMap.getOrDefault(instrumentToken, Collections.emptyList());

	}

	public Optional<Candle> latest(Long instrumentToken) {

		List<Candle> candles = get(instrumentToken);

		if (candles.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(candles.get(candles.size() - 1));

	}

	public void clear(Long instrumentToken) {

		candleMap.remove(instrumentToken);

	}

}