package com.market.commander.quant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSymbolsRequestDto {

    private Long strategySessionId;

    private List<String> symbols;

    private Boolean isActive;
}
