package com.banknifty.market.websocket;

import com.banknifty.market.instrument.InstrumentRegistry;
import com.banknifty.market.realtime.TickAggregator;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Tick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processes incoming WebSocket ticks.
 *
 * Responsibilities: 1. Validate incoming ticks 2. Cache latest tick 3. Update
 * live market store 4. Resolve instrument metadata 5. Forward tick to
 * TickAggregator
 */
@Slf4j
@Component
public class TickProcessor {

	private final LiveMarketStore liveMarketStore;

	private final TickAggregator tickAggregator;

	private final InstrumentRegistry instrumentRegistry;

	/**
	 * Latest Tick Cache
	 */
	private final Map<Long, Tick> latestTicks = new ConcurrentHashMap<>();

	public TickProcessor(LiveMarketStore liveMarketStore, TickAggregator tickAggregator,
			InstrumentRegistry instrumentRegistry) {

		this.liveMarketStore = liveMarketStore;
		this.tickAggregator = tickAggregator;
		this.instrumentRegistry = instrumentRegistry;
	}

	/**
	 * Process incoming tick.
	 */
	public void process(Tick tick) {

		if (tick == null) {
			return;
		}

		if (tick.getInstrumentToken() <= 0) {

			log.warn("Ignoring invalid tick : {}", tick);

			return;
		}

		latestTicks.put(tick.getInstrumentToken(), tick);

		liveMarketStore.updateTick(tick);

		Instrument instrument = instrumentRegistry.getByToken(tick.getInstrumentToken());

		if (instrument == null) {

			log.debug("Instrument not registered : {}", tick.getInstrumentToken());

			return;
		}

		try {

			tickAggregator.onTick(tick);

		} catch (Exception ex) {

			log.error("Tick aggregation failed for instrument {}", tick.getInstrumentToken(), ex);

		}

	}

	/**
	 * Latest Tick.
	 */
	public Tick getLatestTick(Long instrumentToken) {

		return latestTicks.get(instrumentToken);

	}

	/**
	 * Latest LTP.
	 */
	public double getLastTradedPrice(Long instrumentToken) {

		Tick tick = latestTicks.get(instrumentToken);

		return tick == null ? 0.0 : tick.getLastTradedPrice();

	}

	/**
	 * Tick available?
	 */
	public boolean contains(Long instrumentToken) {

		return latestTicks.containsKey(instrumentToken);

	}

	/**
	 * Total cached instruments.
	 */
	public int size() {

		return latestTicks.size();

	}

	/**
	 * Remove cached tick.
	 */
	public void remove(Long instrumentToken) {

		latestTicks.remove(instrumentToken);

	}

	/**
	 * Clear cache.
	 */
	public void clear() {

		latestTicks.clear();

		log.info("Tick cache cleared.");

	}

}