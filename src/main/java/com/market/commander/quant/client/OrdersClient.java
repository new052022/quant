package com.market.commander.quant.client;

import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.CloseOrdersRequestDto;
import com.market.commander.quant.dto.CreateOrderRequestDto;
import com.market.commander.quant.dto.GetAssetsDataRequestDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "orders-client", url = "${orders-service.url}")
public interface OrdersClient {

    @PostMapping("/order-execution/open-orders")
    List<OpenOrderResponseDto> getOpenOrders(GetAssetsDataRequestDto request);

    @PostMapping("/order-execution/open-positions")
    List<OpenPositionResponseDto> getOpenPositions(GetAssetsDataRequestDto request);

    @PostMapping("/order-execution/balance")
    List<AccountBalanceDto> getBalance(GetAssetsDataRequestDto request);

    @PostMapping("/order-execution/perpetual-order")
    OrderResponseDto openOrder(CreateOrderRequestDto request);

    @DeleteMapping("/order-execution")
    ResponseEntity<HttpStatus> deleteOrders(CloseOrdersRequestDto request);

}
