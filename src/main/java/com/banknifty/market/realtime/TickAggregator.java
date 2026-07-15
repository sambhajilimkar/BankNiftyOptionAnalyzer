package com.banknifty.market.realtime;

import com.banknifty.events.CandleClosedEvent;
import com.banknifty.market.instrument.InstrumentRegistry;
import com.banknifty.model.Candle;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Tick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TickAggregator {

	private final CandleBuilder candleBuilder;

	private final CandleCache candleCache;

	private final ActiveCandleManager activeCandleManager;

	private final InstrumentRegistry instrumentRegistry;

	private final ApplicationEventPublisher publisher;

	public TickAggregator(CandleBuilder candleBuilder, CandleCache candleCache, ActiveCandleManager activeCandleManager,
			InstrumentRegistry instrumentRegistry, ApplicationEventPublisher publisher) {

		this.candleBuilder = candleBuilder;
		this.candleCache = candleCache;
		this.activeCandleManager = activeCandleManager;
		this.instrumentRegistry = instrumentRegistry;
		this.publisher = publisher;
	}

	/**
	 * Process one live tick.
	 */
	public void onTick(Tick tick) {

		if (tick == null) {
			return;
		}

		Instrument instrument = instrumentRegistry.getByToken(tick.getInstrumentToken());

		if (instrument == null) {

			log.debug("Instrument not found : {}", tick.getInstrumentToken());

			return;
		}

		Long instrumentToken = tick.getInstrumentToken();

		Candle candle = activeCandleManager.get(instrumentToken).orElse(null);

		try {

			if (candle == null) {

				candle = candleBuilder.newCandle(

						tick,

						instrument.getTradingsymbol(),

						instrument.getExchange(),

						"1minute");

				log.debug("New candle created : {}", instrument.getTradingsymbol());

			} else {

				candle = candleBuilder.update(candle, tick);

			}

			activeCandleManager.put(candle);

		} catch (Exception ex) {

			log.error("Failed to aggregate tick for {}", instrument.getTradingsymbol(), ex);

		}

	}

	/**
	 * Close active candle.
	 */
	public void close(Long instrumentToken) {

		Candle candle = activeCandleManager.remove(instrumentToken);

		if (candle == null) {
			return;
		}

		try {

			Candle completed = candleBuilder.complete(candle);

			candleCache.add(instrumentToken, completed);

			publisher.publishEvent(new CandleClosedEvent(completed));

			log.debug("Candle closed : {} {}", completed.tradingSymbol(), completed.dateTime());

		} catch (Exception ex) {

			log.error("Unable to close candle {}", instrumentToken, ex);

		}

	}

	/**
	 * Current active candle.
	 */
	public Candle active(Long instrumentToken) {

		return activeCandleManager.get(instrumentToken).orElse(null);

	}

	/**
	 * Active candle exists?
	 */
	public boolean hasActiveCandle(Long instrumentToken) {

		return activeCandleManager.get(instrumentToken).isPresent();

	}

}