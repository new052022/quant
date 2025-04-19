package com.market.commander.quant.service;

import com.market.commander.quant.client.MarketClient;
import com.market.commander.quant.dto.AssetContractResponseDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.StopLossTakeProfitPrice;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.entities.StrategySessionSymbol;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakeProfitStopLossService {

    private final StrategySessionService strategySessionService;

    private final StrategyResultsService strategyResultsService;

    private final OrderService orderService;

    private final UsersService usersService;

    private final MarketClient marketClient;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateTakeProfitStopLossOrders() {
        log.info("Start TPSL orders updating");
        List<StrategySession> activeSessions = strategySessionService.findByStatus(SessionStatus.ACTIVE);
        activeSessions.forEach(session -> {
            try {
                log.info("Start updating TPSL for session id {}", session.getId());
                Map<String, StrategySessionSymbol> inactiveSymbols = session.getSymbols().stream()
                        .filter(symbol -> !Boolean.TRUE.equals(symbol.getIsActive()))
                        .collect(Collectors.toMap(symbol -> symbol.getSymbol().getName(), Function.identity()));
                log.info("The size of excluded symbols is: {}", inactiveSymbols.size());
                List<AssetContractResponseDto> assetsByParams = marketClient.getAssetsByParams(session.getExchange());
                Map<String, AssetContractResponseDto> paramsBySymbol = assetsByParams.stream()
                        .collect(Collectors.toMap(AssetContractResponseDto::getSymbol, Function.identity()));
                UserResponseDto userDetails = usersService.getUserDetails(session.getUser().getExternalId(), session.getExchange());
                List<OpenOrderResponseDto> openOrders = orderService.getOpenOrders(userDetails);
                log.info("Size of open orders: {}", openOrders.size());
                Map<String, List<OpenOrderResponseDto>> ordersIdToClose = this.getTPSLOrdersIds(openOrders, session.getSymbols());
                log.info("Size of open orders to close: {}", ordersIdToClose.size());
                orderService.closeOrders(ordersIdToClose, session);
                List<OpenPositionResponseDto> openPositions = orderService.getOpenPositions(userDetails).stream()
                        .filter(position -> !inactiveSymbols.containsKey(position.getSymbol()))
                        .toList();
                log.info("Size of open positions: {}", openPositions.size());
                Map<String, Pair<Pair<Double, Double>, Boolean>> positionSizesBySymbol = this.getPositionSizeBySymbol(openPositions);
                List<StopLossTakeProfitPrice> tpslPrices = strategyResultsService.getStrategyTPSLResults(
                        session, positionSizesBySymbol);
                orderService.createTPSLOrders(paramsBySymbol, tpslPrices, positionSizesBySymbol, userDetails);
            } catch (Exception e) {
                log.error("Failed to open TPSL orders for session with id {} with message: {}", session.getId(), e.getMessage());
            }
        });
        log.info("Finished TPSL orders updating");
    }

    private Map<String, Pair<Pair<Double, Double>, Boolean>> getPositionSizeBySymbol(List<OpenPositionResponseDto> openPositions) {
        return openPositions.stream()
                .collect(groupingBy(
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

    private Map<String, List<OpenOrderResponseDto>> getTPSLOrdersIds(List<OpenOrderResponseDto> openOrders,
                                                                     Set<StrategySessionSymbol> symbols) {
        Set<String> symbolsToSkip = symbols.stream()
                .filter(symbol -> !Boolean.TRUE.equals(symbol.getIsActive()))
                .map(symbol -> symbol.getSymbol().getName())
                .collect(Collectors.toSet());
        return openOrders.stream()
                .filter(this::isStopOrTakeProfitOrder)
                .filter(order -> !symbolsToSkip.contains(order.getSymbol()))
                .collect(groupingBy(OpenOrderResponseDto::getSymbol));
    }

    private boolean isStopOrTakeProfitOrder(OpenOrderResponseDto order) {
        return order.getType().equalsIgnoreCase(BinanceOrderType.STOP.name()) ||
                order.getType().equalsIgnoreCase(BinanceOrderType.TAKE_PROFIT.name());
    }

}
