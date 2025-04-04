package com.market.commander.quant.client;

import com.market.commander.quant.dto.StrategyParamsRequestDto;
import com.market.commander.quant.dto.StrategyResultsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "strategy-client", url = "${strategy-service.url}")
public interface StrategyClient {

    @PostMapping
    StrategyResultsResponseDto getStrategyResults(StrategyParamsRequestDto params);

}
