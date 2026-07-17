package com.banknifty.recommendation.service;

import com.banknifty.broker.model.OptionQuote;
import com.banknifty.config.TradingProperties;
import com.banknifty.enums.OptionType;
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

	private static final long MIN_OPEN_INTEREST = 100L;
	private static final long MIN_VOLUME = 10L;

	private final OptionCandidateMapper mapper;
	private final TradingProperties tradingProperties;

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
		if (quote.ltp() == null || quote.ltp().signum() <= 0) {
			return false;
		}
		log.info("DEBUG accepting contract {} {} LTP={} VOL={} OI={}", quote.strike(), quote.optionType(), quote.ltp(), quote.volume(), quote.openInterest());
		return true;
	}
}
