package com.banknifty.optionchain;

import com.banknifty.optionchain.model.OptionChain;

import java.time.LocalDate;

public interface OptionChainService {

	OptionChain load(

			String underlying,

			LocalDate expiry

	);

}