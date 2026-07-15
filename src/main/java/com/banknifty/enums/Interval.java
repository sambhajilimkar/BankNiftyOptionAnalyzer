package com.banknifty.enums;

public enum Interval {

    MINUTE("minute"),

    THREE_MINUTE("3minute"),

    FIVE_MINUTE("5minute"),

    TEN_MINUTE("10minute"),

    FIFTEEN_MINUTE("15minute"),

    THIRTY_MINUTE("30minute"),

    SIXTY_MINUTE("60minute"),

    DAY("day");

    private final String value;

    Interval(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}