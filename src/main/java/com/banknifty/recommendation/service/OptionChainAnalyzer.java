package com.banknifty.recommendation.service;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
import com.banknifty.recommendation.config.TradeCandidateProperties;
import com.banknifty.recommendation.mapper.OptionCandidateMapper;
import com.banknifty.recommendation.model.OptionCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionChainAnalyzer {

	private final OptionCandidateMapper mapper;
	private final TradingProperties tradingProperties;
	private final TradeCandidateProperties candidateProperties;

	public List<OptionCandidate> analyze(List<OptionQuote> optionChain, BigDecimal spotPrice) {

		if (optionChain == null || optionChain.isEmpty() || spotPrice == null || spotPrice.signum() <= 0) {
			return List.of();
		}

		List<OptionCandidate> candidates = optionChain.stream().filter(this::isTradable)
				.map(option -> mapper.map(option, spotPrice)).toList();

		if (candidates.isEmpty()) {
			return List.of();
		}

		BigDecimal minimumDistance = candidates.stream().map(OptionCandidate::getDistanceFromATM)
				.min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

		candidates.forEach(candidate -> classifyStrike(candidate, spotPrice, minimumDistance));

		return candidates.stream().sorted(Comparator.comparing(OptionCandidate::getDistanceFromATM)
				.thenComparing(OptionCandidate::getTradingSymbol)).toList();
	}

	private void classifyStrike(OptionCandidate candidate, BigDecimal spotPrice, BigDecimal minimumDistance) {

		BigDecimal strike = BigDecimal.valueOf(candidate.getStrike());
		boolean atm = candidate.getDistanceFromATM().compareTo(minimumDistance) == 0;
		boolean itm = !atm && ((candidate.getOptionType() == OptionType.CE && strike.compareTo(spotPrice) < 0)
				|| (candidate.getOptionType() == OptionType.PE && strike.compareTo(spotPrice) > 0));

		candidate.setAtm(atm);
		candidate.setItm(itm);
		candidate.setOtm(!atm && !itm);
		int strikeStep = Math.max(tradingProperties.getStrikeStep(), 1);
		candidate.setStrikeDistance(candidate.getDistanceFromATM()
				.divide(BigDecimal.valueOf(strikeStep), 0, RoundingMode.HALF_UP).intValue());
	}

	private boolean isTradable(OptionQuote quote) {

		if (quote == null || quote.strike() == null || quote.optionType() == null) {
			return false;
		}
		if (quote.ltp() == null || quote.ltp().signum() <= 0 || quote.bid() == null || quote.ask() == null
				|| quote.bid().signum() <= 0 || quote.ask().compareTo(quote.bid()) < 0) {
			return false;
		}
		if (quote.volume() == null || quote.volume() < candidateProperties.getMinimumVolume()
				|| quote.openInterest() == null || quote.openInterest() < candidateProperties.getMinimumOpenInterest()) {
			return false;
		}
		if (quote.ltp().doubleValue() < candidateProperties.getMinimumPremium()
				|| quote.ltp().doubleValue() > candidateProperties.getMaximumPremium()) {
			return false;
		}
		BigDecimal spreadPercent = quote.ask().subtract(quote.bid()).multiply(BigDecimal.valueOf(100))
				.divide(quote.ltp(), 4, RoundingMode.HALF_UP);
		return spreadPercent.doubleValue() <= candidateProperties.getMaximumSpread();
	}
}
