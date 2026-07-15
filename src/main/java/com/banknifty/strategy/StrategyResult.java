package com.banknifty.strategy;

import com.banknifty.enums.SignalType;
import lombok.Builder;

@Builder
public record StrategyResult(

		SignalType signal,

		int score,

		double confidence,

		String reason

) {
}