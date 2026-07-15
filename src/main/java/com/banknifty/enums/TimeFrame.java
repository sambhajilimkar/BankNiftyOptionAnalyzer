package com.banknifty.enums;

public enum TimeFrame {

    ONE_MINUTE("minute", 1),

    FIVE_MINUTE("5minute", 5),

    FIFTEEN_MINUTE("15minute", 15),

    THIRTY_MINUTE("30minute", 30),

    ONE_HOUR("60minute", 60),

    ONE_DAY("day", 1440);

    private final String kiteInterval;
    private final int minutes;

    TimeFrame(String kiteInterval, int minutes) {
        this.kiteInterval = kiteInterval;
        this.minutes = minutes;
    }

    public String kiteInterval() {
        return kiteInterval;
    }

    public int minutes() {
        return minutes;
    }
}