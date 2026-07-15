package com.banknifty.market.websocket;

import com.zerodhatech.models.Tick;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for latest live market data.
 */
@Component
public class LiveMarketStore {

	/**
	 * instrumentToken -> Latest Tick
	 */
	private final Map<Long, Tick> latestTicks = new ConcurrentHashMap<>();

	/**
	 * Update latest tick.
	 */
	public void updateTick(Tick tick) {

		if (tick == null) {
			return;
		}

		latestTicks.put(tick.getInstrumentToken(), tick);

	}

	/**
	 * Get latest tick.
	 */
	public Tick getTick(Long instrumentToken) {

		return latestTicks.get(instrumentToken);

	}

	/**
	 * Check if instrument exists.
	 */
	public boolean contains(Long instrumentToken) {

		return latestTicks.containsKey(instrumentToken);

	}

	/**
	 * Remove one instrument.
	 */
	public void remove(Long instrumentToken) {

		latestTicks.remove(instrumentToken);

	}

	/**
	 * Remove everything.
	 */
	public void clear() {

		latestTicks.clear();

	}

	/**
	 * Total subscribed instruments.
	 */
	public int size() {

		return latestTicks.size();

	}

	/**
	 * All latest ticks.
	 */
	public Collection<Tick> getAllTicks() {

		return latestTicks.values();

	}

}