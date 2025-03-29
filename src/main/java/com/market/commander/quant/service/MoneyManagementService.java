package com.market.commander.quant.service;

import com.market.commander.quant.dto.OpenPositionDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategySession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoneyManagementService {

    private final OrderService orderService;

    public void checkOrdersConditions(List<Order> orders, StrategySession session) {
        List<OpenPositionDto> positions = orderService.getOpenOrdersAndPositions(session.getUser());
    }
}
