package com.market.commander.quant.mapper;

import com.market.commander.quant.dto.StrategyResultsResponseDto;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class StrategyResultsMapper {

    public List<StrategyResult> toStrategyResults(StrategyResultsResponseDto strategyResults, StrategySession session) {
        return strategyResults.getOrdersList().stream()
                .map(result -> StrategyResult.builder()
                        .session(session)
                        .side(result.getSide())
                        .minEntryPrice(result.getMinEntryPrice())
                        .symbol(result.getSymbol())
                        .stopPrice(result.getStopPrice())
                        .build())
                .toList();
    }
}
