package com.banknifty.analysis.reversal.detector;

import com.banknifty.analysis.reversal.ReversalDirection;
import com.banknifty.analysis.reversal.model.SwingPoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DivergenceDetector {

	public ReversalDirection detectBullish(List<SwingPoint> lows) {

		if (lows == null || lows.size() < 2) {
			return ReversalDirection.NONE;
		}

		SwingPoint previous = lows.get(lows.size() - 2);
		SwingPoint latest = lows.get(lows.size() - 1);

		boolean priceLowerLow = latest.getPrice() < previous.getPrice();
		boolean indicatorHigherLow = latest.getIndicator() > previous.getIndicator();

		if (priceLowerLow && indicatorHigherLow) {
			return ReversalDirection.BULLISH;
		}

		return ReversalDirection.NONE;
	}

	public ReversalDirection detectBearish(List<SwingPoint> highs) {

		if (highs == null || highs.size() < 2) {
			return ReversalDirection.NONE;
		}

		SwingPoint previous = highs.get(highs.size() - 2);
		SwingPoint latest = highs.get(highs.size() - 1);

		boolean priceHigherHigh = latest.getPrice() > previous.getPrice();
		boolean indicatorLowerHigh = latest.getIndicator() < previous.getIndicator();

		if (priceHigherHigh && indicatorLowerHigh) {
			return ReversalDirection.BEARISH;
		}

		return ReversalDirection.NONE;
	}
}