package com.banknifty.service;

import com.banknifty.model.Recommendation;

public interface RecommendationEngine {

    Recommendation analyze(String index);

}