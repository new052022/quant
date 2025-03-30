package com.market.commander.quant.service;

import com.market.commander.quant.client.OrdersClient;
import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.GetAssetsDataRequestDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.enums.OrderStatus;
import com.market.commander.quant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrdersClient ordersClient;

    @Transactional
    public List<Order> createOrders(List<StrategyResult> results) {
        List<Order> newOrders = results.stream()
                .map(result -> Order.builder()
                        .status(OrderStatus.NEW)
                        .params(result)
                        .build())
                .toList();
        return this.saveOrders(newOrders);
    }

    public List<Order> saveOrders(List<Order> newOrders) {
        return orderRepository.saveAll(newOrders);
    }

    public void openOrders(List<Order> orders) {

    }

    public List<OpenOrderResponseDto> getOpenOrdersAndPositions(UserResponseDto userDetails) {
        return ordersClient.getOpenOrders(GetAssetsDataRequestDto.builder()
                .encodedApiKey(userDetails.getApiKey())
                .encodedSecretKey(userDetails.getSecretKey())
                .exchange(userDetails.getExchangeName())
                .build());
    }

    public List<OpenPositionResponseDto> getOpenPositions(UserResponseDto userDetails) {
        return ordersClient.getOpenPositions(GetAssetsDataRequestDto.builder()
                .encodedApiKey(userDetails.getApiKey())
                .encodedSecretKey(userDetails.getSecretKey())
                .exchange(userDetails.getExchangeName())
                .build());
    }

    public List<AccountBalanceDto> getBalances(UserResponseDto userDetails) {
        return ordersClient.getBalance(GetAssetsDataRequestDto.builder()
                .encodedApiKey(userDetails.getApiKey())
                .encodedSecretKey(userDetails.getSecretKey())
                .exchange(userDetails.getExchangeName())
                .build());
    }
}
