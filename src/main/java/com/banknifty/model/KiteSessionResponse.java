package com.banknifty.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record KiteSessionResponse(

		boolean success,

		String userId,

		String userName,

		LocalDateTime loginTime,

		boolean active,

		String message

) {
}