package com.market.commander.quant.Service;

import com.market.commander.quant.client.OrdersClient;
import com.market.commander.quant.dto.OpenPositionDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.entities.User;
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
    public List<Order> createOrders(List<StrategyResult> results, StrategySession session) {
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

    public List<OpenPositionDto> getOpenOrdersAndPositions(User user) {
        List<OpenPositionResponseDto> openPositions = ordersClient.getOpenPositions(user.getExternalId());

    }
}
