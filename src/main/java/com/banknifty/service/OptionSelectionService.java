package com.banknifty.service;

import com.banknifty.model.OptionContract;
import com.banknifty.model.TrendScore;

import java.util.List;

public interface OptionSelectionService {

    OptionContract selectBestOption(

            TrendScore trendScore,

            double bankNiftySpot,

            List<OptionContract> contracts

    );

}