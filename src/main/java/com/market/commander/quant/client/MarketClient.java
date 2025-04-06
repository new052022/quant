package com.market.commander.quant.client;

import com.market.commander.quant.dto.AssetContractResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "marketData", url = "${market-data.url}")
public interface MarketClient {

    @GetMapping("/asset-price/${market-data.asset-details}")
    List<AssetContractResponseDto> getAssetsByParams(String exchange);

}
