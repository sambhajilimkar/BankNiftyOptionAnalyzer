package com.banknifty.exception;

/**
 * Generic exception for all Gateway related failures.
 */
@SuppressWarnings("serial")
public class GatewayException extends RuntimeException {

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}