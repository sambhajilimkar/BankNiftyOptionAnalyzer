package com.banknifty.optionchain.model;

import lombok.Builder;

@Builder
public record OptionMetrics(

		double pcr,

		Integer maxPain,

		Integer atmStrike,

		long totalCallOI,

		long totalPutOI,

		long callVolume,

		long putVolume

) {
}