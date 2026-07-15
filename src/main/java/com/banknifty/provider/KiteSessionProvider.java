package com.banknifty.provider;

import com.banknifty.exception.GatewayException;
import com.banknifty.gateway.GatewayHealthService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiteSessionProvider {

	private final KiteConnect kiteConnect;

	private final GatewayHealthService healthService;

	@Value("${kite.api-secret}")
	private String apiSecret;

	/**
	 * Generate a new Kite session.
	 */
	public User generateSession(String requestToken) {

		try {

			log.info("Generating Kite Session...");

			User user = null;
			try {
				user = kiteConnect.generateSession(requestToken, apiSecret);
			} catch (KiteException e) {
				System.out.println(e.message);
				e.printStackTrace();
			}

			kiteConnect.setAccessToken(user.accessToken);
			kiteConnect.setUserId(user.userId);

			healthService.markSuccess();

			log.info("Kite Session Generated Successfully for {}", user.userId);

			return user;

		} catch (Exception ex) {

			healthService.markFailure(ex);

			throw new GatewayException("Unable to generate Kite session.", ex);

		}

	}

	/**
	 * Restore an existing session.
	 */
	public void configureSession(String accessToken, String userId) {

		kiteConnect.setAccessToken(accessToken);
		kiteConnect.setUserId(userId);

		healthService.markSuccess();

		log.info("Kite Session Restored for {}", userId);

	}

	/**
	 * Current Kite instance.
	 */
	public KiteConnect getKiteConnect() {
		return kiteConnect;
	}

}