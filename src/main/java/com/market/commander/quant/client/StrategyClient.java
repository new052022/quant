package com.market.commander.quant.client;

import com.market.commander.quant.config.FeignConfig;
import com.market.commander.quant.dto.StopLossTakeProfitPrice;
import com.market.commander.quant.dto.StrategyParamsRequestDto;
import com.market.commander.quant.dto.StrategyResultsResponseDto;
import com.market.commander.quant.dto.TpSlStrategyRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "strategy-client", url = "${strategy-service.url}", configuration = FeignConfig.class)
public interface StrategyClient {

    @PostMapping("${strategy-service.run-strategy}")
    StrategyResultsResponseDto getStrategyResults(StrategyParamsRequestDto params);

    @PostMapping("/tpsl")
    List<StopLossTakeProfitPrice> getTpSlResults(@RequestBody List<TpSlStrategyRequestDto> tpslRequest);

}
