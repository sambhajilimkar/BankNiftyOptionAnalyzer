package com.banknifty.optionchain;

import com.banknifty.optionchain.model.OptionSnapshot;

public interface OptionSnapshotProvider {

	OptionSnapshot load(

			String underlying,

			String expiryType

	);

}