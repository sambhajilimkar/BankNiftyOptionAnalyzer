package com.banknifty.market;

import com.zerodhatech.ticker.OnError;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketManager {

	/**
	 * BANKNIFTY Index Token
	 */
	private static final long BANKNIFTY_TOKEN = 260105L;

	private final KiteTickerProvider tickerProvider;

	private KiteTicker ticker;

	@PostConstruct
	public void initialize() {

		try {

			ticker = tickerProvider.getTicker();

			registerListeners();

		} catch (Exception ex) {

			log.error("Unable to initialize WebSocket.", ex);

		}

	}

	/**
	 * Connect WebSocket.
	 */
	public void connect() {

		if (ticker == null) {
			return;
		}

		log.info("Connecting KiteTicker...");

		ticker.connect();

	}

	/**
	 * Disconnect.
	 */
	public void disconnect() {

		if (ticker != null) {

			ticker.disconnect();

			log.info("KiteTicker disconnected.");

		}

	}

	/**
	 * Subscribe BANKNIFTY.
	 */
	public void subscribeBankNifty() {

		ArrayList<Long> tokens = new ArrayList<>();

		tokens.add(BANKNIFTY_TOKEN);

		ticker.subscribe(tokens);

		ticker.setMode(tokens, KiteTicker.modeFull);

		log.info("Subscribed : BANKNIFTY");

	}

	/**
	 * Register SDK callbacks.
	 */
	private void registerListeners() {

		ticker.setOnConnectedListener(() -> {

			log.info("WebSocket Connected.");

			subscribeBankNifty();

		});

		ticker.setOnDisconnectedListener(() -> log.warn("WebSocket Disconnected."));

		ticker.setOnErrorListener(new OnError() {

			@Override
			public void onError(Exception exception) {

				log.error("WebSocket Exception", exception);

			}

			@Override
			public void onError(KiteException kiteException) {

				log.error("Kite Exception : {}", kiteException.message);

			}

			@Override
			public void onError(String error) {

				log.error("WebSocket Error : {}", error);

			}

		});

		ticker.setOnTickerArrivalListener(WebSocketManager.this::onTicks);

	}

	/**
	 * Tick callback.
	 */
	private void onTicks(ArrayList<Tick> ticks) {

		if (ticks == null || ticks.isEmpty()) {
			return;
		}

		log.debug("Received {} ticks.", ticks.size());

		// TickProcessor will be integrated in next file.

	}

}