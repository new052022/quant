package com.market.commander.quant.service;

import com.market.commander.quant.client.OrdersClient;
import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.CreateOrderRequestDto;
import com.market.commander.quant.dto.GetAssetsDataRequestDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.OrderResponseDto;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.OrderStatus;
import com.market.commander.quant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrdersClient ordersClient;

    private final UsersService usersService;

    @Transactional
    public List<Order> createOrders(List<StrategyResult> results) {
        List<Order> newOrders = results.stream()
                .map(result -> Order.builder()
                        .status(OrderStatus.NEW)
                        .openPrice(result.getMinEntryPrice())
                        .params(result)
                        .build())
                .toList();
        return this.saveOrders(newOrders);
    }

    public List<Order> saveOrders(List<Order> newOrders) {
        return orderRepository.saveAll(newOrders);
    }

    @Transactional
    public void openOrders(List<Order> orders, StrategySession session) {
        List<Order> openedOrders = new ArrayList<>();
        Long userExternalId = session.getUser().getExternalId();
        String exchange = session.getExchange();
        UserResponseDto userDetails = usersService.getUserDetails(userExternalId, exchange);
        orders.forEach(order -> {
            try {
                CreateOrderRequestDto request = this.buildOrderRequest(order, exchange, userDetails);
                ordersClient.openOrder(request);
                order.setStatus(OrderStatus.OPEN);
                openedOrders.add(order);
            } catch (Exception e) {
                log.error("Error during opening the order with id {} with message: {}", order.getId(), e.getMessage());
            }
            orderRepository.saveAll(openedOrders);
        });

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

    private static CreateOrderRequestDto buildOrderRequest(Order order, String exchange, UserResponseDto userDetails) {
        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setExchange(exchange);
        request.setSymbol(order.getParams().getSymbol());
        request.setPrice(order.getOpenPrice().toString());
        request.setQuantity(order.getSize().toString());
        request.setSide(order.getParams().getSide());
        request.setType("LIMIT");
        request.setTimeInForce("GTC");
        request.setApiKey(userDetails.getApiKey());
        request.setPrivateKey(userDetails.getSecretKey());
        request.setNewClientOrderId(order.getId().toString());
        return request;
    }
}
