package com.market.commander.quant.client;

import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.GetAssetsDataRequestDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "orders-client", url = "${orders-service.url}")
public interface OrdersClient {

    @PostMapping("/order_execution/open-orders")
    List<OpenOrderResponseDto> getOpenOrders(GetAssetsDataRequestDto request);

    @PostMapping("/order_execution/open-positions")
    List<OpenPositionResponseDto> getOpenPositions(GetAssetsDataRequestDto request);

    @PostMapping("/order-execution/balance")
    List<AccountBalanceDto> getBalance(GetAssetsDataRequestDto request);
}
