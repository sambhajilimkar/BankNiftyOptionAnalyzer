package com.banknifty.gateway;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GatewayHealthService {

	@Getter
	private volatile boolean connected = false;

	@Getter
	private volatile LocalDateTime lastSuccessfulCall;

	@Getter
	private volatile LocalDateTime lastFailedCall;

	@Getter
	private volatile String lastError = "";

	/**
	 * Mark Gateway UP.
	 */
	public void markSuccess() {

		connected = true;
		lastSuccessfulCall = LocalDateTime.now();
		lastError = "";

		log.debug("Gateway Status : CONNECTED");

	}

	/**
	 * Mark Gateway DOWN.
	 */
	public void markFailure(Exception ex) {

		connected = false;
		lastFailedCall = LocalDateTime.now();

		if (ex != null) {
			lastError = ex.getMessage();
			log.error("Gateway Failure : {}", ex.getMessage(), ex);
		}

	}

	/**
	 * Reset state.
	 */
	public void reset() {

		connected = false;
		lastSuccessfulCall = null;
		lastFailedCall = null;
		lastError = "";

	}

}