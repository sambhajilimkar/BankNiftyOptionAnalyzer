package com.banknifty.service.impl;

import com.banknifty.enums.TimeFrame;
import com.banknifty.model.MultiTimeFrameAnalysis;
import com.banknifty.model.TimeFrameAnalysis;
import com.banknifty.model.TrendDirection;
import com.banknifty.service.MultiTimeFrameAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MultiTimeFrameAnalysisServiceImpl implements MultiTimeFrameAnalysisService {

	@Override
	public MultiTimeFrameAnalysis analyze(String tradingSymbol) {

		/*
		 * In the next iteration, replace this placeholder with calls to AnalysisService
		 * for each timeframe:
		 *
		 * 1m 5m 15m 60m
		 */

		List<TimeFrameAnalysis> results = new ArrayList<>();

		results.add(TimeFrameAnalysis.builder().timeFrame(TimeFrame.ONE_MINUTE).trend(TrendDirection.BULLISH).score(78)
				.bullish(true).bearish(false).confirmed(true).build());

		results.add(TimeFrameAnalysis.builder().timeFrame(TimeFrame.FIVE_MINUTE).trend(TrendDirection.STRONG_BULLISH)
				.score(92).bullish(true).bearish(false).confirmed(true).build());

		results.add(TimeFrameAnalysis.builder().timeFrame(TimeFrame.FIFTEEN_MINUTE).trend(TrendDirection.BULLISH)
				.score(86).bullish(true).bearish(false).confirmed(true).build());

		results.add(TimeFrameAnalysis.builder().timeFrame(TimeFrame.ONE_HOUR).trend(TrendDirection.BULLISH).score(83)
				.bullish(true).bearish(false).confirmed(true).build());

		int overall = (78 + 92 + 86 + 83) / 4;

		return MultiTimeFrameAnalysis.builder()

				.analyses(results)

				.overallScore(overall)

				.bullish(overall >= 75)

				.bearish(overall <= 30)

				.confirmed(overall >= 75)

				.build();

	}

}