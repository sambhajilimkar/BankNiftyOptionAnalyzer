package com.banknifty.exception;

@SuppressWarnings("serial")
public class InstrumentNotFoundException extends MarketDataException {

    public InstrumentNotFoundException(String symbol) {

        super(
                ErrorCode.INSTRUMENT_NOT_FOUND,
                "Instrument not found : " + symbol
        );

    }

}