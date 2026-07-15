package com.banknifty.analysis;

import com.banknifty.events.CandleClosedEvent;
import com.banknifty.indicator.result.IndicatorSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndicatorPipelineListener {

	private final IndicatorPipeline indicatorPipeline;

	@EventListener
	public void onCandleClosed(CandleClosedEvent event) {

		try {

			IndicatorSnapshot snapshot = indicatorPipeline.calculate(event.candle().instrumentToken());

			log.info("Indicators calculated for {}", event.candle().tradingSymbol());

		} catch (Exception ex) {

			log.error("Indicator calculation failed for {}", event.candle().tradingSymbol(), ex);

		}

	}

}