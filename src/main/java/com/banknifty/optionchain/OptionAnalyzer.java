package com.banknifty.optionchain;

import com.banknifty.optionchain.model.OptionSnapshot;

public interface OptionAnalyzer {

	OptionSnapshot analyse(OptionSnapshot snapshot);

}