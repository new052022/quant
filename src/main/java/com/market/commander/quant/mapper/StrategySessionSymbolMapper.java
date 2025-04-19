package com.market.commander.quant.mapper;

import com.market.commander.quant.dto.SessionSymbolsResponseDto;
import com.market.commander.quant.dto.StrategySessionSymbolDto;
import com.market.commander.quant.entities.StrategySessionSymbol;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class StrategySessionSymbolMapper {

    public SessionSymbolsResponseDto toSessionSymbolsResponse(List<StrategySessionSymbol> symbolList) {
        return SessionSymbolsResponseDto.builder()
                .symbols(this.toSymbolsDto(symbolList))
                .build();
    }

    private List<StrategySessionSymbolDto> toSymbolsDto(List<StrategySessionSymbol> symbolList) {
        return symbolList.stream()
                .map(symbol -> StrategySessionSymbolDto.builder()
                        .id(symbol.getId())
                        .strategySessionId(symbol.getStrategySession().getId())
                        .symbol(symbol.getSymbol().getName())
                        .isActive(symbol.getIsActive())
                        .build())
                .toList();
    }
}
