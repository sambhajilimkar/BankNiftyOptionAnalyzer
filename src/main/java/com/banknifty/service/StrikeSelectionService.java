package com.banknifty.service;

import com.banknifty.enums.SignalType;
import com.banknifty.model.OptionRecommendation;
import com.banknifty.provider.KiteOptionChainProvider;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
public class StrikeSelectionService {

	private final KiteOptionChainProvider optionChainProvider;

	public StrikeSelectionService(KiteOptionChainProvider optionChainProvider) {
		this.optionChainProvider = optionChainProvider;
	}

	/**
	 * Select nearest ATM strike.
	 */
	public Instrument selectATMStrike(OptionRecommendation recommendation, String exchange, String underlying,
			double spotPrice) throws KiteException, IOException, JSONException {

		List<Instrument> options = optionChainProvider.getOptionContracts(exchange, underlying);

		SignalType signal = recommendation.signal();

		return options.stream()

				.filter(i -> {

					if (signal == SignalType.BUY_CE) {
						return i.tradingsymbol.endsWith("CE");
					}

					if (signal == SignalType.BUY_PE) {
						return i.tradingsymbol.endsWith("PE");
					}

					return false;

				})

				.min(Comparator.comparingDouble(i ->

				Math.abs(parseStrike(i.getStrike()) - spotPrice)

				))

				.orElse(null);

	}

	/**
	 * Safely parse strike because Zerodha SDK stores it as String.
	 */
	private double parseStrike(String strike) {

		if (strike == null || strike.isBlank()) {
			return 0;
		}

		try {

			return Double.parseDouble(strike);

		} catch (NumberFormatException ex) {

			return 0;

		}

	}

}