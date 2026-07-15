package com.banknifty.recommendation.engine;

import com.banknifty.recommendation.model.StrikeCandidate;
import com.banknifty.recommendation.model.TradeSetup;

public interface TradeSetupBuilder {

	TradeSetup build(StrikeCandidate candidate);

}