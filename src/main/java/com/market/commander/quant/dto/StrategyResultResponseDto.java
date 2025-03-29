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

}
