package com.banknifty.analysis.reversal;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ReversalResult {

	private double score;

	private ReversalDirection direction = ReversalDirection.NONE;

	private final List<ReversalReason> reasons = new ArrayList<>();

	public void addScore(double value) {
		score += value;
	}

	public void addReason(ReversalReason reason) {
		reasons.add(reason);
	}

	public void setDirection(ReversalDirection direction) {
		this.direction = direction;
	}

}