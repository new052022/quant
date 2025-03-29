package com.market.commander.quant.client;

import com.market.commander.quant.dto.StrategyParamsRequestDto;
import com.market.commander.quant.dto.StrategyResultsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "strategy-client", url = "${strategy-service.url}")
public interface StrategyClient {

    @GetMapping("${users-service.user-exchange-endpoint}/{userId}")
    StrategyResultsResponseDto getStrategyResults(StrategyParamsRequestDto params);

}
