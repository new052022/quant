package com.market.commander.quant.mapper;

import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.entities.User;
import com.market.commander.quant.service.UsersService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class StrategySessionMapper {

    @Autowired
    private UsersService usersService;

    @Mapping(target = "user", source = "userId", qualifiedByName = "toUser")
    public abstract StrategySession toStrategySession(RunStrategyRequestDto request);

    public abstract RunSessionResponseDto toRunSessionResponse(StrategySession savedSession);

    @Named("toUser")
    public User toUser(Long userId) {
        return usersService.getById(userId).orElse(usersService.create(userId));
    }
}
