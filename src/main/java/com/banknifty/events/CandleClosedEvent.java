package com.banknifty.events;

import com.banknifty.model.Candle;

public record CandleClosedEvent(

		Candle candle

) {
}