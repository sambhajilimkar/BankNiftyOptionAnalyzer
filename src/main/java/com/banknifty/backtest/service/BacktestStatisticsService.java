package com.banknifty.backtest.service;

import com.banknifty.backtest.entity.TradeOutcome;
import com.banknifty.backtest.entity.TradeStatus;
import com.banknifty.backtest.model.BacktestStatistics;
import com.banknifty.backtest.repository.TradeOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BacktestStatisticsService {

	private final TradeOutcomeRepository repository;

	public BacktestStatistics calculate() {

		List<TradeOutcome> trades = repository.findAll();

		if (trades.isEmpty()) {
			return BacktestStatistics.empty();
		}

		long totalTrades = trades.size();

		long winners = trades.stream().filter(this::winner).count();

		long losers = trades.stream().filter(t -> t.getStatus() == TradeStatus.STOPLOSS_HIT).count();

		BigDecimal totalProfit = trades.stream().map(TradeOutcome::getPnl).filter(p -> p != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		double winRate = totalTrades == 0 ? 0 : (winners * 100.0) / totalTrades;

		return BacktestStatistics.builder().totalTrades(totalTrades).winningTrades(winners).losingTrades(losers)
				.winRate(winRate).totalProfit(totalProfit.setScale(2, RoundingMode.HALF_UP)).build();
	}

	private boolean winner(TradeOutcome trade) {

		return trade.getStatus() == TradeStatus.TARGET1_HIT || trade.getStatus() == TradeStatus.TARGET2_HIT
				|| trade.getStatus() == TradeStatus.EXITED;
	}

}