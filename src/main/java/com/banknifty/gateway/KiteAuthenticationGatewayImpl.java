package com.banknifty.gateway;

import com.banknifty.exception.GatewayException;
import com.banknifty.provider.KiteSessionProvider;
import com.zerodhatech.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiteAuthenticationGatewayImpl implements KiteAuthenticationGateway {

	private final KiteSessionProvider sessionProvider;

	private volatile boolean authenticated = false;

	@Override
	public User generateSession(String requestToken) {

		try {

			User user = sessionProvider.generateSession(requestToken);

			authenticated = true;

			log.info("Kite Session Generated Successfully : {}", user.userId);

			return user;

		} catch (Exception ex) {

			authenticated = false;

			throw new GatewayException("Unable to generate Kite Session.", ex);

		}

	}

	@Override
	public void restoreSession(String accessToken, String userId) {

		sessionProvider.configureSession(accessToken, userId);

		authenticated = true;

		log.info("Kite Session Restored : {}", userId);

	}

	@Override
	public boolean isAuthenticated() {

		return authenticated;

	}

}