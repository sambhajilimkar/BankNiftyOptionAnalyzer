package com.banknifty.broker;

import com.banknifty.broker.model.LiveQuote;
import com.banknifty.broker.model.OptionQuote;
import com.banknifty.model.Candle;

import java.time.LocalDateTime;
import java.util.List;

public interface BrokerProvider {

	String brokerName();

	boolean isConnected();

	List<Candle> historicalData(Long instrumentToken, String interval, LocalDateTime from, LocalDateTime to);

	LiveQuote quote(Long instrumentToken);

	List<OptionQuote> optionChain(String underlying, String expiry);

}