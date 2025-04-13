package com.market.commander.quant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopLossTakeProfitPrice {

    private String symbol;

    private Double stopLoss;

    private Double takeProfit;

    private Long userId;

    private String exchange;

}
