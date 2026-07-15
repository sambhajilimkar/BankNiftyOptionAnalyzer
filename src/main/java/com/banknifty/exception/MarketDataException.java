package com.banknifty.exception;

@SuppressWarnings("serial")
public class MarketDataException extends RuntimeException {

    private final ErrorCode errorCode;

    public MarketDataException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MarketDataException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}