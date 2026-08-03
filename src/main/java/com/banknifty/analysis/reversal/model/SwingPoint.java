package com.banknifty.analysis.reversal.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SwingPoint {

    private final int index;

    private final double price;

    private final double indicator;

}