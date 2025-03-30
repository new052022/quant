package com.market.commander.quant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResultResponseDto {

    private Double minEntryPrice;

    private String symbol;

    private String side;

    private Double stopPrice;

    @Override
    public String toString() {
        return "StrategyResultResponseDto{" +
                "minEntryPrice=" + minEntryPrice +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", stopPrice=" + stopPrice +
                '}';
    }

}
