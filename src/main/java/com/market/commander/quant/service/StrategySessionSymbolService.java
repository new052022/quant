package com.market.commander.quant.service;

import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.entities.StrategySessionSymbol;
import com.market.commander.quant.entities.Symbol;
import com.market.commander.quant.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategySessionSymbolService {

    public List<StrategySessionSymbol> buildStrategySessionSymbols(StrategySession session,
                                                                   List<Symbol> symbols, Boolean isActive) {
        return symbols.stream()
                .map(symbol -> this.buildStrategySessionSymbol(symbol, session, isActive))
                .toList();
    }

    public void updateOrdersWithExcludedSymbols(StrategySession session, List<Order> orders) {
        Map<String, StrategySessionSymbol> sessionsBySymbol = session.getSymbols().stream()
                .collect(Collectors.toMap(symbol -> symbol.getSymbol().getName(), Function.identity()));
        orders.forEach(order -> {
            StrategySessionSymbol sessionSymbol = sessionsBySymbol.getOrDefault(order.getParams().getSymbol(), null);
            if (Objects.nonNull(sessionSymbol) && !Boolean.TRUE.equals(sessionSymbol.getIsActive())) {
                order.setStatus(OrderStatus.CANCELLED);
            }
        });
    }

    private StrategySessionSymbol buildStrategySessionSymbol(Symbol symbol, StrategySession session, Boolean isActive) {
        return StrategySessionSymbol.builder()
                .strategySession(session)
                .symbol(symbol)
                .isActive(isActive)
                .build();
    }

}
