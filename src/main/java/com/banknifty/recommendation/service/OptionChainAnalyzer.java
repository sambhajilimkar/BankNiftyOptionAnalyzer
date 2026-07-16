package com.banknifty.recommendation.service;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.recommendation.mapper.OptionCandidateMapper;
import com.banknifty.recommendation.model.OptionCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionChainAnalyzer {

	private static final long MIN_OPEN_INTEREST = 100L;

	private static final long MIN_VOLUME = 10L;

	private final OptionCandidateMapper mapper;

	public List<OptionCandidate> analyze(List<OptionQuote> optionChain, BigDecimal spotPrice) {

		return optionChain.stream()

				.filter(this::isTradable)

				.map(option -> mapper.map(option, spotPrice))

				.sorted(Comparator.comparing(OptionCandidate::getDistanceFromATM))

				.toList();

	}

	private boolean isTradable(OptionQuote quote) {

		if (quote == null) {
			return false;
		}

		if (quote.ltp() == null || quote.ltp().signum() <= 0) {
			return false;
		}

		if (quote.volume() == null || quote.volume() < MIN_VOLUME) {
			return false;
		}

		if (quote.openInterest() == null || quote.openInterest() < MIN_OPEN_INTEREST) {
			return false;
		}

		if (quote.bid() == null || quote.ask() == null) {
			return false;
		}

		return true;
	}

}