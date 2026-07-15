package com.banknifty.market.websocket;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnError;
import com.zerodhatech.ticker.OnTicks;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KiteWebSocketService implements DisposableBean {

	private final TickProcessor tickProcessor;

	private KiteTicker ticker;

	public KiteWebSocketService(TickProcessor tickProcessor) {
		this.tickProcessor = tickProcessor;
	}

	public void connect(String apiKey, String accessToken, List<Long> instrumentTokens) {

		// IMPORTANT:
		// KiteTicker constructor is (accessToken, apiKey)
		ticker = new KiteTicker(accessToken, apiKey);

		ticker.setOnConnectedListener(new OnConnect() {

			@Override
			public void onConnected() {

				System.out.println("Connected to Zerodha WebSocket.");

				ArrayList<Long> tokens = new ArrayList<>(instrumentTokens);

				ticker.subscribe(tokens);

				ticker.setMode(tokens, KiteTicker.modeFull);

			}
		});

		ticker.setOnDisconnectedListener(new OnDisconnect() {

			@Override
			public void onDisconnected() {

				System.out.println("Disconnected from Zerodha WebSocket.");

			}

		});

		ticker.setOnTickerArrivalListener(new OnTicks() {

			@Override
			public void onTicks(ArrayList<Tick> ticks) {

				for (Tick tick : ticks) {

					tickProcessor.process(tick);

				}

			}

		});

		ticker.setOnErrorListener(new OnError() {

			@Override
			public void onError(Exception exception) {

				exception.printStackTrace();

			}

			@Override
			public void onError(KiteException exception) {

				exception.printStackTrace();

			}

			@Override
			public void onError(String message) {

				System.err.println(message);

			}

		});

		ticker.connect();

	}

	public boolean isConnected() {

		return ticker != null && ticker.isConnectionOpen();

	}

	public void disconnect() {

		if (ticker != null) {

			ticker.disconnect();

		}

	}

	@Override
	public void destroy() {

		disconnect();

	}

}