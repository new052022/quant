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

    @Override
    public String toString() {
        return new StringBuilder("StopLossTakeProfitPrice{")
                .append("symbol='").append(symbol).append("', ")
                .append("stopLoss=").append(stopLoss).append(", ")
                .append("takeProfit=").append(takeProfit).append(", ")
                .append("userId=").append(userId).append(", ")
                .append("exchange='").append(exchange).append("'")
                .append("}")
                .toString();
    }

}
