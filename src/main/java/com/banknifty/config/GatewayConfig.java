package com.banknifty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

	/**
	 * Enable Gateway Layer.
	 */
	private boolean enabled = true;

	/**
	 * Enable retry.
	 */
	private boolean retryEnabled = true;

	/**
	 * Retry attempts.
	 */
	private int retryCount = 3;

	/**
	 * Retry interval (milliseconds).
	 */
	private long retryDelay = 1000L;

	/**
	 * Request timeout.
	 */
	private int timeout = 30000;

	/**
	 * Logging.
	 */
	private boolean loggingEnabled = true;

}