package com.banknifty.recommendation.model;

import java.util.List;

import com.banknifty.enums.OptionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the consolidated technical signal produced by the indicator
 * pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signal {

	/**
	 * Recommended option direction.
	 */
	private OptionType optionType;

	/**
	 * Technical confidence (0-100).
	 */
	private int confidence;

	/**
	 * Reasons contributing to the signal.
	 */
	private List<String> reasons;
}