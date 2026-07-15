package com.banknifty.service;

import com.banknifty.model.MultiTimeFrameAnalysis;

public interface MultiTimeFrameAnalysisService {

    MultiTimeFrameAnalysis analyze(String tradingSymbol);

}