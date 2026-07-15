package com.banknifty.config;

import com.zerodhatech.kiteconnect.KiteConnect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Zerodha Kite Connect.
 */
@Configuration
public class KiteConnectConfig {

	@Value("${kite.api-key}")
	private String apiKey;

	@Value("${kite.user-id:}")
	private String userId;

	@Value("${kite.access-token:}")
	private String accessToken;

	@Bean
	public KiteConnect kiteConnect() {

		try {

			KiteConnect kiteConnect = new KiteConnect(apiKey);

			if (accessToken != null && !accessToken.isBlank()) {
				kiteConnect.setAccessToken(accessToken);
			} else {
				System.err.println("Kite access token is not configured. Live market APIs will be unavailable.");
			}

			if (userId != null && !userId.isBlank()) {
				kiteConnect.setUserId(userId);
			}

			return kiteConnect;

		} catch (Throwable t) {

			t.printStackTrace(); // <-- This is important

			throw t;
		}
	}
}
