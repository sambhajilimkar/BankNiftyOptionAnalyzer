package com.banknifty.market.realtime;

import com.banknifty.model.Candle;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains currently forming candle for each instrument.
 */
@Component
public class ActiveCandleManager {

	private final Map<Long, Candle> activeCandles = new ConcurrentHashMap<>();

	public Optional<Candle> get(Long instrumentToken) {

		return Optional.ofNullable(activeCandles.get(instrumentToken));

	}

	public void put(Candle candle) {

		activeCandles.put(candle.instrumentToken(), candle);

	}

	public Candle remove(Long instrumentToken) {

		return activeCandles.remove(instrumentToken);

	}

	public boolean contains(Long instrumentToken) {

		return activeCandles.containsKey(instrumentToken);

	}

	public Map<Long, Candle> getAll() {

		return activeCandles;

	}

}