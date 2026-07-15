package com.banknifty.model;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Generic Gateway response.
 */
@Builder
public record GatewayResponse(

        boolean success,

        String message,

        LocalDateTime timestamp

) {

    public static GatewayResponse success(String message) {

        return GatewayResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

    }

    public static GatewayResponse failure(String message) {

        return GatewayResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

    }

}