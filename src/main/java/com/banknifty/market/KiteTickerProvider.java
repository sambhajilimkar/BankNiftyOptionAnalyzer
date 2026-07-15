package com.banknifty.market;

import com.banknifty.provider.KiteSessionProvider;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.ticker.KiteTicker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiteTickerProvider {

	private final KiteSessionProvider sessionProvider;

	private KiteTicker kiteTicker;

	/**
	 * Creates KiteTicker using current access token.
	 */
	public synchronized KiteTicker getTicker() {

		if (kiteTicker != null) {
			return kiteTicker;
		}

		KiteConnect kiteConnect = sessionProvider.getKiteConnect();

		kiteTicker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

		kiteTicker.setTryReconnection(true);

		try {
			kiteTicker.setMaximumRetries(50);

			kiteTicker.setMaximumRetryInterval(30);
		} catch (KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("KiteTicker initialized.");

		return kiteTicker;

	}

	/**
	 * Current ticker instance.
	 */
	public KiteTicker currentTicker() {

		return kiteTicker;

	}

	/**
	 * Connected ?
	 */
	public boolean isConnected() {

		return kiteTicker != null && kiteTicker.isConnectionOpen();

	}

}