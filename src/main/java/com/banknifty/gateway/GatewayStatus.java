package com.banknifty.gateway;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Current Gateway status.
 */
@Builder
public record GatewayStatus(

		boolean connected,

		boolean authenticated,

		String userId,

		String broker,

		String environment,

		LocalDateTime loginTime,

		LocalDateTime lastHeartbeat,

		String message

) {
}