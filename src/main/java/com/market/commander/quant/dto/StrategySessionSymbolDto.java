package com.market.commander.quant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategySessionSymbolDto {

    private Long id;

    private Long strategySessionId;

    private String symbol;

    private Boolean isActive;

}
