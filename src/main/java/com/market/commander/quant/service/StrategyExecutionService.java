package com.market.commander.quant.service;

import com.market.commander.quant.dto.WebsocketChannelResultResponse;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.OrderStatus;
import com.market.commander.quant.enums.SessionStatus;
import com.market.commander.quant.enums.StreamStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutionService {

    private final StrategySessionSymbolService strategySessionSymbolService;

    private final ExchangeStreamingService exchangeStreamingService;

    private final StrategySessionService strategySessionService;

    private final StrategyResultsService strategyResultsService;

    private final MoneyManagementService moneyManagementService;

    private final OrderService orderService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void runActiveSessions() {
        List<StrategySession> sessionsToClose = new ArrayList<>();
        List<StrategySession> sessionsToSave = new ArrayList<>();
        log.info("Run sessions at {} ", LocalDateTime.now());
        List<StrategySession> sessions = strategySessionService.findByStatus(SessionStatus.ACTIVE);
        sessions.forEach(session -> {
            try {
                if (this.checkSessionStatus(session)) {
                    List<StrategyResult> results = strategyResultsService.getStrategyResults(session);
                    if (CollectionUtils.isEmpty(results)) {
                        log.info("The results array is empty from strategy service");
                        return;
                    }
                    List<Order> orders = orderService.createOrders(results);
                    moneyManagementService.checkOrdersConditions(orders, session);
                    strategySessionSymbolService.updateOrdersWithExcludedSymbols(session, orders);
                    log.info("Starting to open orders (count): {}", orders.size());
                    orderService.openOrders(orders, session);
                    this.openWebsocketChannel(orders, session);
                    sessionsToSave.add(session);
                } else {
                    sessionsToClose.add(session);
                }
            } catch (Exception e) {
                log.error("Failed to run session with id: {} with message: {}", session.getId(), e.getMessage());
            }
        });
        strategySessionService.saveAll(sessionsToSave);
        strategySessionService.closeSessions(sessionsToClose);
        log.info("End sessions running at {} ", LocalDateTime.now());
    }

    private void openWebsocketChannel(List<Order> orders, StrategySession session) {
        if ((!CollectionUtils.isEmpty(orders) &&
                orders.stream().anyMatch(order -> order.getStatus() == OrderStatus.OPEN))
                && !session.getIsStreamConnectionOpened()) {
            WebsocketChannelResultResponse response = exchangeStreamingService.openWebsocketConnection(session.getUser().getId(),
                    session.getId(), session.getExchange());
            session.setIsStreamConnectionOpened(response.getType() == StreamStatusType.ACTIVE);
        }
    }

    private Boolean checkSessionStatus(StrategySession session) {
        return session.getEndTime().isAfter(LocalDateTime.now());
    }
}
