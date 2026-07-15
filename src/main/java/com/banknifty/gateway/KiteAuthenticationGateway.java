package com.banknifty.gateway;

import com.zerodhatech.models.User;

/**
 * Authentication operations exposed by Gateway.
 */
public interface KiteAuthenticationGateway {

	/**
	 * Generate new session.
	 */
	User generateSession(String requestToken);

	/**
	 * Restore an existing session.
	 */
	void restoreSession(String accessToken, String userId);

	/**
	 * Whether current gateway has an active session.
	 */
	boolean isAuthenticated();

}