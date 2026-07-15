package com.banknifty.optionchain.impl;

import com.banknifty.optionchain.OptionAnalyzer;
import com.banknifty.optionchain.model.OptionMetrics;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import org.springframework.stereotype.Component;

@Component
public class DefaultOptionAnalyzer implements OptionAnalyzer {

	@Override
	public OptionSnapshot analyse(OptionSnapshot snapshot) {

		long totalCallOI = snapshot.calls().stream().mapToLong(OptionStrike::openInterest).sum();

		long totalPutOI = snapshot.puts().stream().mapToLong(OptionStrike::openInterest).sum();

		long callVolume = snapshot.calls().stream().mapToLong(OptionStrike::volume).sum();

		long putVolume = snapshot.puts().stream().mapToLong(OptionStrike::volume).sum();

		double pcr = totalCallOI == 0 ? 0 : (double) totalPutOI / totalCallOI;

		OptionMetrics metrics = OptionMetrics.builder()

				.pcr(pcr)

				.atmStrike(snapshot.atmStrike())

				.maxPain(snapshot.atmStrike()) // temporary

				.totalCallOI(totalCallOI)

				.totalPutOI(totalPutOI)

				.callVolume(callVolume)

				.putVolume(putVolume)

				.build();

		return OptionSnapshot.builder()

				.underlying(snapshot.underlying())

				.expiry(snapshot.expiry())

				.spotPrice(snapshot.spotPrice())

				.atmStrike(snapshot.atmStrike())

				.itmCallStrike(snapshot.itmCallStrike())

				.itmPutStrike(snapshot.itmPutStrike())

				.otmCallStrike(snapshot.otmCallStrike())

				.otmPutStrike(snapshot.otmPutStrike())

				.calls(snapshot.calls())

				.puts(snapshot.puts())

				.metrics(metrics)

				.build();

	}

}