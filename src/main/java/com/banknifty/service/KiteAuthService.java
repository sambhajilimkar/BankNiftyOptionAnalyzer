package com.banknifty.service;

import com.banknifty.model.KiteLoginResponse;
import com.banknifty.model.KiteSessionResponse;

public interface KiteAuthService {

	/**
	 * Returns Zerodha login URL.
	 */
	KiteLoginResponse login();

	/**
	 * Generates access token from request token.
	 */
	KiteSessionResponse callback(String requestToken);

	/**
	 * Active session.
	 */
	KiteSessionResponse currentSession();

	/**
	 * Logout.
	 */
	void logout();

}