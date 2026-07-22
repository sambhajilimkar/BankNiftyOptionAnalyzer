package com.banknifty.optionchain.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.enums.OptionType;
import com.banknifty.enums.ExpiryType;
import com.banknifty.enums.RiskProfile;
import com.banknifty.enums.TradingStyle;
import com.banknifty.optionchain.mapper.OptionStrikeMapper;
import com.banknifty.optionchain.model.OptionMetrics;
import com.banknifty.optionchain.model.OptionSnapshot;
import com.banknifty.optionchain.model.OptionStrike;
import com.banknifty.optionchain.service.OptionSnapshotService;
import com.banknifty.options.service.OptionUniverseService;
import com.banknifty.recommendation.model.RecommendationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionSnapshotServiceImpl implements OptionSnapshotService {

	private static final String UNDERLYING = "BANKNIFTY";

	private final OptionUniverseService optionUniverseService;

	private final OptionStrikeMapper optionStrikeMapper;

	@Override
	public OptionSnapshot getLatestSnapshot() {

		RecommendationRequest request = RecommendationRequest.builder().instrument(UNDERLYING)
				.expiryType(ExpiryType.WEEKLY).tradingStyle(TradingStyle.INTRADAY).riskProfile(RiskProfile.MODERATE)
				.capital(100000d).build();

		return getLatestSnapshot(request, null);
	}

	@Override
	public OptionSnapshot getLatestSnapshot(RecommendationRequest request, BigDecimal liveSpotPrice) {

		if (request == null) {
			return null;
		}

		List<OptionQuote> quotes = optionUniverseService.loadUniverse(request);

		if (quotes == null || quotes.isEmpty()) {

			log.warn("Broker returned empty option chain.");

			return null;
		}

		List<OptionStrike> strikes = optionStrikeMapper.toStrikes(quotes);

		List<OptionStrike> calls = strikes.stream().filter(s -> s.optionType() == OptionType.CE)
				.sorted(Comparator.comparing(OptionStrike::strike)).collect(Collectors.toList());

		List<OptionStrike> puts = strikes.stream().filter(s -> s.optionType() == OptionType.PE)
				.sorted(Comparator.comparing(OptionStrike::strike)).collect(Collectors.toList());

		if (calls.isEmpty() || puts.isEmpty()) {

			log.warn("Incomplete option chain.");

			return null;
		}

		BigDecimal spotPrice = liveSpotPrice == null || liveSpotPrice.signum() <= 0 ? estimateSpotPrice(calls, puts)
				: liveSpotPrice;

		Integer atmStrike = determineATMStrike(strikes, spotPrice);

		OptionMetrics metrics = buildMetrics(calls, puts, atmStrike);

		return OptionSnapshot.builder().underlying(request.instrument()).expiry(calls.get(0).expiry()).spotPrice(spotPrice)
				.atmStrike(atmStrike).itmCallStrike(atmStrike - 100).otmCallStrike(atmStrike + 100)
				.itmPutStrike(atmStrike + 100).otmPutStrike(atmStrike - 100).metrics(metrics).calls(calls).puts(puts)
				.build();
	}

	private BigDecimal estimateSpotPrice(List<OptionStrike> calls, List<OptionStrike> puts) {

		OptionStrike atm = calls.stream().min(Comparator.comparing(s -> s.ltp().abs())).orElse(calls.get(0));

		return BigDecimal.valueOf(atm.strike());
	}

	private Integer determineATMStrike(List<OptionStrike> strikes, BigDecimal spotPrice) {

		return strikes.stream().min(Comparator.comparing(s -> BigDecimal.valueOf(s.strike()).subtract(spotPrice).abs()))
				.map(OptionStrike::strike).orElse(0);
	}

	private OptionMetrics buildMetrics(List<OptionStrike> calls, List<OptionStrike> puts, Integer atmStrike) {

		long callOI = calls.stream().mapToLong(OptionStrike::openInterest).sum();

		long putOI = puts.stream().mapToLong(OptionStrike::openInterest).sum();

		long callVolume = calls.stream().mapToLong(OptionStrike::volume).sum();

		long putVolume = puts.stream().mapToLong(OptionStrike::volume).sum();

		double pcr = callOI == 0 ? 0 : (double) putOI / callOI;

		return OptionMetrics.builder().atmStrike(atmStrike).pcr(pcr).totalCallOI(callOI).totalPutOI(putOI)
				.callVolume(callVolume).putVolume(putVolume).maxPain(0).build();
	}

}
