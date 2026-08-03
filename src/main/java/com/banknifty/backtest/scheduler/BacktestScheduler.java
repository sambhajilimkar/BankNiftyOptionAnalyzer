package com.banknifty.backtest.scheduler;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.banknifty.backtest.entity.TradeOutcome;
import com.banknifty.backtest.entity.TradeStatus;
import com.banknifty.backtest.repository.TradeOutcomeRepository;
import com.banknifty.broker.BrokerProvider;
import com.banknifty.broker.model.LiveQuote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BacktestScheduler {

	private final TradeOutcomeRepository tradeOutcomeRepository;

	private final BrokerProvider brokerProvider;

	/**
	 * Every minute check all open trades.
	 */
	@Scheduled(fixedDelay = 60000)
	public void evaluateOpenTrades() {

		List<TradeOutcome> openTrades = tradeOutcomeRepository.findByStatus(TradeStatus.OPEN);

		for (TradeOutcome trade : openTrades) {

			try {

				String symbol = trade.getRecommendation().getTradingSymbol();

				Long instrumentToken = trade.getRecommendation().getInstrumentToken();

				LiveQuote quote = brokerProvider.quote(instrumentToken);

				if (quote == null) {
					continue;
				}

				BigDecimal latestPremium = quote.ltp();

				if (latestPremium == null) {
					continue;
				}

				if (latestPremium == null) {
					continue;
				}

				trade.setHighestPrice(max(trade.getHighestPrice(), latestPremium));

				trade.setLowestPrice(min(trade.getLowestPrice(), latestPremium));

				if (trade.getTarget2Hit() == null && trade.getRecommendation().getTarget2() != null
						&& latestPremium.compareTo(trade.getRecommendation().getTarget2()) >= 0) {

					trade.setTarget2Hit(true);
					trade.setStatus(TradeStatus.TARGET2_HIT);
				}

				else if (trade.getTarget1Hit() == null && trade.getRecommendation().getTarget1() != null
						&& latestPremium.compareTo(trade.getRecommendation().getTarget1()) >= 0) {

					trade.setTarget1Hit(true);
					trade.setStatus(TradeStatus.TARGET1_HIT);
				}

				else if (trade.getRecommendation().getStopLoss() != null
						&& latestPremium.compareTo(trade.getRecommendation().getStopLoss()) <= 0) {

					trade.setStopLossHit(true);
					trade.setStatus(TradeStatus.STOPLOSS_HIT);
				}

				tradeOutcomeRepository.save(trade);

			} catch (Exception ex) {

				log.error("Backtest evaluation failed", ex);
			}
		}

		log.info("Backtest evaluated {} open trades", openTrades.size());
	}

	private BigDecimal max(BigDecimal a, BigDecimal b) {

		if (a == null)
			return b;
		if (b == null)
			return a;

		return a.max(b);
	}

	private BigDecimal min(BigDecimal a, BigDecimal b) {

		if (a == null)
			return b;
		if (b == null)
			return a;

		return a.min(b);
	}

}