package com.market.commander.quant.service;

import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.StopLossTakeProfitPrice;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.BinanceOrderType;
import com.market.commander.quant.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakeProfitStopLossService {

    private final StrategySessionService strategySessionService;

    private final StrategyResultsService strategyResultsService;

    private final OrderService orderService;

    private final UsersService usersService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateTakeProfitStopLossOrders() {
        List<StrategySession> activeSessions = strategySessionService.findByStatus(SessionStatus.ACTIVE);
        activeSessions.forEach(session -> {
            try {
                UserResponseDto userDetails = usersService.getUserDetails(session.getUser().getExternalId(), session.getExchange());
                List<OpenOrderResponseDto> openOrders = orderService.getOpenOrders(userDetails);
                Map<String, String> ordersIdToClose = this.getTPSLOrdersIds(openOrders);
//                orderService.closeOrders(ordersIdToClose, session);
//                List<OpenPositionResponseDto> openPositions = orderService.getOpenPositions(userDetails);
//                Map<String, Pair<Pair<Double, Double>, Boolean>> positionSizesBySymbol = this.getPositionSizeBySymbol(openPositions);
//                List<StopLossTakeProfitPrice> tpslPrices = strategyResultsService.getStrategyTPSLResults(
//                        session, positionSizesBySymbol);
//                orderService.createTPSLOrders(tpslPrices, positionSizesBySymbol);
            } catch (Exception e) {
                log.error("Failed to open TPSL orders for session with id {} with message: {}", session.getId(), e.getMessage());
            }
        });

    }

    private Map<String, Pair<Pair<Double, Double>, Boolean>> getPositionSizeBySymbol(List<OpenPositionResponseDto> openPositions) {
        return openPositions.stream()
                .collect(Collectors.groupingBy(
                        OpenPositionResponseDto::getSymbol, // Группируем по символу
                        Collectors.collectingAndThen(
                                Collectors.reducing(
                                        Pair.of(Pair.of(BigDecimal.ZERO, BigDecimal.ZERO), Boolean.FALSE), // Начальное значение: Pair<Pair<размер, суммарная стоимость>, флаг LONG>
                                        position -> {
                                            BigDecimal size = new BigDecimal(position.getPositionAmt());
                                            BigDecimal entryPrice = new BigDecimal(position.getEntryPrice());
                                            boolean isLong = "LONG".equalsIgnoreCase(position.getPositionSide()) ||
                                                    "BOTH".equalsIgnoreCase(position.getPositionSide());
                                            return Pair.of(
                                                    Pair.of(size.abs(), size.abs().multiply(entryPrice)), // Размер и стоимость позиции
                                                    isLong // Флаг LONG/SHORT
                                            );
                                        },
                                        (pair1, pair2) -> {
                                            BigDecimal totalSize = pair1.getFirst().getFirst().add(pair2.getFirst().getFirst());
                                            BigDecimal totalCost = pair1.getFirst().getSecond().add(pair2.getFirst().getSecond());
                                            boolean isLong = pair1.getSecond() || pair2.getSecond();
                                            return Pair.of(
                                                    Pair.of(totalSize, totalCost),
                                                    isLong
                                            );
                                        }
                                ),
                                pair -> {
                                    BigDecimal totalSize = pair.getFirst().getFirst();
                                    BigDecimal averageEntryPrice = totalSize.compareTo(BigDecimal.ZERO) == 0
                                            ? BigDecimal.ZERO
                                            : pair.getFirst().getSecond().divide(totalSize, 8, RoundingMode.HALF_UP); // Средняя цена входа
                                    return Pair.of(
                                            Pair.of(
                                                    totalSize.doubleValue(), // Размер позиции
                                                    averageEntryPrice.doubleValue() // Цена входа
                                            ),
                                            pair.getSecond() // Флаг LONG/SHORT
                                    );
                                }
                        )
                ));
    }

    private Map<String, String> getTPSLOrdersIds(List<OpenOrderResponseDto> openOrders) {
        return openOrders.stream()
                .filter(this::isStopOrTakeProfitOrder)
                .collect(Collectors.toMap(OpenOrderResponseDto::getSymbol,
                        OpenOrderResponseDto::getClientOrderId));
    }

    private boolean isStopOrTakeProfitOrder(OpenOrderResponseDto order) {
        return order.getType().equalsIgnoreCase(BinanceOrderType.STOP.name()) ||
                order.getType().equalsIgnoreCase(BinanceOrderType.TAKE_PROFIT.name());
    }

}
