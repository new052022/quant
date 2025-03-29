package com.market.commander.quant.mapper;

import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import com.market.commander.quant.entities.StrategySession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StrategySessionMapper {

    StrategySession toStrategySession(RunStrategyRequestDto request);

    RunSessionResponseDto toRunSessionResponse(StrategySession savedSession);
}
