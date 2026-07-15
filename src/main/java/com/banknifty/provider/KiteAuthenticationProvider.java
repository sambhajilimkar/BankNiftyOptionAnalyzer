package com.banknifty.provider;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles Zerodha Kite authentication and session management.
 */
@Component
public class KiteAuthenticationProvider {

	private final KiteConnect kiteConnect;

	@Value("${kite.api-secret}")
	private String apiSecret;

	public KiteAuthenticationProvider(KiteConnect kiteConnect) {
		this.kiteConnect = kiteConnect;
	}

	/**
	 * Generates a Kite session using the request token.
	 *
	 * @param requestToken Request token received after Zerodha login.
	 * @return Authenticated User session.
	 * @throws Exception if authentication fails.
	 * @throws KiteException 
	 */
	public User login(String requestToken) throws Exception, KiteException {

		if (requestToken == null || requestToken.isBlank()) {
			throw new IllegalArgumentException("Request token cannot be null or empty.");
		}

		User user = kiteConnect.generateSession(requestToken, apiSecret);

		// Store tokens in KiteConnect client
		kiteConnect.setAccessToken(user.accessToken);
		kiteConnect.setPublicToken(user.publicToken);

		return user;
	}

	/**
	 * Returns logged-in user profile.
	 * @throws KiteException 
	 */
	public Profile getProfile() throws Exception, KiteException {
		return kiteConnect.getProfile();
	}

	/**
	 * Returns configured KiteConnect instance.
	 */
	public KiteConnect getKiteConnect() {
		return kiteConnect;
	}

	/**
	 * Returns current access token.
	 */
	public String getAccessToken() {
		return kiteConnect.getAccessToken();
	}

	/**
	 * Returns current user id.
	 */
	public String getUserId() {
		return kiteConnect.getUserId();
	}

	/**
	 * Returns Zerodha login URL.
	 */
	public String getLoginUrl() {
		return kiteConnect.getLoginURL();
	}

	/**
	 * Clears local session tokens.
	 */
	public void logout() {
		kiteConnect.setAccessToken(null);
		kiteConnect.setPublicToken(null);
	}

	/**
	 * Checks whether user is authenticated.
	 */
	public boolean isAuthenticated() {
		String token = kiteConnect.getAccessToken();
		return token != null && !token.isBlank();
	}
}