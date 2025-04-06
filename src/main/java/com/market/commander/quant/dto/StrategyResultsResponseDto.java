package com.market.commander.quant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResultsResponseDto {

    private List<StrategyResultResponseDto> ordersList = new ArrayList<>();

}
