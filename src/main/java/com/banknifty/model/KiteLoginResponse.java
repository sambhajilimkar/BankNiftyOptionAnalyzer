package com.banknifty.model;

import lombok.Builder;

@Builder
public record KiteLoginResponse(

		String loginUrl,

		String apiKey,

		String redirectUrl

) {
}