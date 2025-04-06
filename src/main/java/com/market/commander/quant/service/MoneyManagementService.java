package com.market.commander.quant.service;

import com.market.commander.quant.client.MarketClient;
import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.AssetContractResponseDto;
import com.market.commander.quant.dto.FilterTypeResonseDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.dto.records.BalanceInfo;
import com.market.commander.quant.dto.records.SessionSettings;
import com.market.commander.quant.dto.records.TradingContext;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.entities.User;
import com.market.commander.quant.enums.OrderStatus;
import com.market.commander.quant.util.RoundNumbers;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoneyManagementService {

    private final OrderService orderService;

    private final UsersService usersService;

    private final MarketClient marketClient;

    private final NotionalCalculator notionalCalculator;

    /**
     * Checks if placing the proposed orders violates money management rules (position limits or sufficient funds).
     * Does NOT modify the orders; returns a map of orders that need status updates.
     *
     * @param ordersToCheck List of new/proposed orders to check.
     * @param session       The strategy session containing rules and user info.
     * @return A Map where keys are Order objects from ordersToCheck that need status update,
     * and values are the target OrderStatus (e.g., POSITION_LIMIT_EXCEEDED, INSUFFICIENT_FUNDS).
     * Returns an empty map if no conditions are violated or checks cannot be performed.
     */
    @Transactional(readOnly = true) // Often, checks don't need write transactions unless fetching lazy data
    public Map<Order, OrderStatus> checkOrdersConditions(@NonNull List<Order> ordersToCheck,
                                                         @NonNull StrategySession session) {
        User user = session.getUser();
        if (user == null || user.getExternalId() == null) {
            log.error("User or externalId is missing for session {}. Cannot perform checks.", session.getId());
            // Indicate failure for all orders - responsibility of caller to handle this map
            return createResultMapForAll(ordersToCheck, OrderStatus.FAILED, "Missing User Info");
        }
        // 2. Fetch and Prepare Context Data
        Optional<TradingContext> contextOpt = this.prepareTradingContext(user, session.getExchange());
        if (contextOpt.isEmpty()) {
            // Error logged within prepareTradingContext
            return createResultMapForAll(ordersToCheck, OrderStatus.FAILED, "Data Fetching Error");
        }
        TradingContext context = contextOpt.get();
        // 3. Extract Essential Info (Balance, Settings)
        Optional<BalanceInfo> usdtBalanceInfoOpt = context.getUsdtBalanceInfo();
        if (usdtBalanceInfoOpt.isEmpty()) {
            log.error("USDT balance not found or invalid for user {}. Cannot perform checks.", user.getExternalId());
            return createResultMapForAll(ordersToCheck, OrderStatus.FAILED, "Missing USDT Balance");
        }
        BalanceInfo usdtBalance = usdtBalanceInfoOpt.get();

        SessionSettings settings = new SessionSettings(session);

        this.setOrdersSize(ordersToCheck, usdtBalance, settings, session);
        // 4. Group Orders to Check
        Map<String, List<Order>> ordersToCheckBySymbol = ordersToCheck.stream()
                .filter(o -> o != null && o.getParams() != null && o.getParams().getSymbol() != null)
                .collect(groupingBy(o -> o.getParams().getSymbol()));
        // 5. Perform Checks Based on Mode (Percent Limit vs Insufficient Funds)
        log.info("Use percent limit checking: {}", settings.usePercentLimit());
        if (settings.usePercentLimit()) {
            return checkPositionLimitPerSymbol(ordersToCheckBySymbol, context, settings, usdtBalance);
        } else {
            return checkInsufficientFunds(ordersToCheck, settings, usdtBalance);
        }
    }

    private void setOrdersSize(@NonNull List<Order> ordersToCheck, BalanceInfo usdtBalance,
                               SessionSettings settings, StrategySession session) {
        List<AssetContractResponseDto> assetsByParams = marketClient.getAssetsByParams(session.getExchange());
        Map<String, AssetContractResponseDto> paramsBySymbol = assetsByParams.stream()
                .collect(Collectors.toMap(AssetContractResponseDto::getSymbol, Function.identity()));
        ordersToCheck.forEach(order -> {
            try {
                log.info("Start setting order size and price for symbol {} with order params: {} size; and price: {}",
                        order.getParams().getSymbol(), order.getSize(), order.getOpenPrice());
                order.setSize((this.getSize(usdtBalance, settings, order, paramsBySymbol)));
                order.setOpenPrice(this.getOpePrice(order, paramsBySymbol));
                log.info("Order price for symbol {} is {}", order.getParams().getSymbol(), order.getSize());
            } catch (Exception e) {
                log.error("Error during asset and price size setting in money management service with message: {}", e.getMessage());
            }
        });
    }

    private Double getOpePrice(Order order, Map<String, AssetContractResponseDto> paramsBySymbol) {
        return RoundNumbers.toOpenPriceByAssetParam(
                paramsBySymbol.get(order.getParams().getSymbol()).getFilters()
                        .stream()
                        .filter(filter -> filter.getFilterType().equalsIgnoreCase("PRICE_FILTER"))
                        .map(FilterTypeResonseDto::getTickSize)
                        .findFirst().orElse(0.01), order.getOpenPrice());
    }

    private BigDecimal getSize(BalanceInfo usdtBalance, SessionSettings settings, Order order,
                               Map<String, AssetContractResponseDto> paramsBySymbol) {
        AssetContractResponseDto assetContractResponseDto = paramsBySymbol.getOrDefault(order.getParams().getSymbol(), null);
        if (Objects.nonNull(assetContractResponseDto)) {
            Double marketLotSize = assetContractResponseDto.getFilters().stream()
                    .filter(filter -> filter.getFilterType().equalsIgnoreCase("MARKET_LOT_SIZE"))
                    .map(FilterTypeResonseDto::getStepSize)
                    .findFirst().orElse(1.0);
            log.info("market lot size is: {}", marketLotSize);
            BigDecimal assetSize = usdtBalance.totalBalance()
                    .add(usdtBalance.unPnl())
                    .multiply(settings.leverage())
                    .multiply(settings.getOrderSizePercentDecimal().get())
                    .divide(BigDecimal.valueOf(order.getOpenPrice()));
            log.info("market lot size: {}; and asset size: {}", marketLotSize, assetSize);
            return RoundNumbers.toAssetSize(marketLotSize, assetSize);
        } else {
            log.error("Symbol params from market data service is missed");
            return null;
        }
    }

    // ========================================================================
    // Data Preparation and Context
    // ========================================================================
    private Optional<TradingContext> prepareTradingContext(User user, String exchange) {
        try {
            UserResponseDto userDetails = usersService.getUserDetails(user.getExternalId(), exchange);
            List<OpenOrderResponseDto> openOrders = orderService.getOpenOrdersAndPositions(userDetails);
            List<OpenPositionResponseDto> openPositions = orderService.getOpenPositions(userDetails);
            List<AccountBalanceDto> balances = orderService.getBalances(userDetails);

            return Optional.of(new TradingContext(balances, openOrders, openPositions));
        } catch (Exception e) {
            log.error("Failed to fetch trading context data for user {} on exchange {}: {}",
                    user.getExternalId(), exchange, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ========================================================================
    // Check Logic Implementations
    // ========================================================================

    private Map<Order, OrderStatus> checkPositionLimitPerSymbol(
            Map<String, List<Order>> ordersToCheckBySymbol,
            TradingContext context,
            SessionSettings settings,
            BalanceInfo usdtBalance) {
        Map<Order, OrderStatus> results = new HashMap<>();
        Optional<BigDecimal> maxPercentDecimalOpt = settings.getMaxPercentDecimal();
        if (maxPercentDecimalOpt.isEmpty()) {
            log.error("Internal error: Max percent decimal is empty while usePercentLimit is true. SessionId: {}", settings); // Log settings or session ID
            ordersToCheckBySymbol.values().forEach(list -> list.forEach(order -> {
                order.setStatus(OrderStatus.FAILED);
                results.put(order, OrderStatus.FAILED);
            }));
            return results;
        }

        BigDecimal maxAllowedNotionalPerSymbol = usdtBalance.totalBalance()
                .multiply(maxPercentDecimalOpt.get())
                .multiply(settings.leverage());

        log.debug("Max allowed notional per symbol: {} (Balance: {}, MaxPercent: {}, Leverage: {})",
                maxAllowedNotionalPerSymbol, usdtBalance.totalBalance(), maxPercentDecimalOpt.get(), settings.leverage());

        Map<String, List<OpenPositionResponseDto>> openPositionsBySymbol = context.getOpenPositionsBySymbol();
        Map<String, List<OpenOrderResponseDto>> openOrdersBySymbol = context.getOpenOrdersBySymbol();

        ordersToCheckBySymbol.forEach((symbol, symbolOrdersToCheck) -> {
            BigDecimal currentPositionsNotional = notionalCalculator.calculateExistingPositionsNotional(
                    openPositionsBySymbol.getOrDefault(symbol, Collections.emptyList())
            );
            BigDecimal currentOpenOrdersNotional = notionalCalculator.calculateOpenOrdersNotional(
                    openOrdersBySymbol.getOrDefault(symbol, Collections.emptyList())
            );
            BigDecimal newOrdersNotional = notionalCalculator.calculateNewOrdersNotional(symbolOrdersToCheck);

            BigDecimal totalPotentialNotional = currentPositionsNotional
                    .add(currentOpenOrdersNotional)
                    .add(newOrdersNotional);

            log.debug("Symbol: {}, Current Pos Notional: {}, Current Orders Notional: {}, New Orders Notional: {}, Total Potential: {}",
                    symbol, currentPositionsNotional, currentOpenOrdersNotional, newOrdersNotional, totalPotentialNotional);

            if (totalPotentialNotional.compareTo(maxAllowedNotionalPerSymbol) > 0) {
                log.warn("Position limit EXCEEDED for symbol {}. Limit: {}, Potential: {}.",
                        symbol, maxAllowedNotionalPerSymbol, totalPotentialNotional);
                symbolOrdersToCheck.forEach(order -> {
                    order.setStatus(OrderStatus.POSITION_LIMIT_EXCEEDED);
                    results.put(order, OrderStatus.POSITION_LIMIT_EXCEEDED);
                });
            } else {
                log.debug("Position limit OK for symbol {}. Limit: {}, Potential: {}",
                        symbol, maxAllowedNotionalPerSymbol, totalPotentialNotional);
                // No status update needed if OK
            }
        });
        return results;
    }

    private Map<Order, OrderStatus> checkInsufficientFunds(
            List<Order> ordersToCheck,
            SessionSettings settings,
            BalanceInfo usdtBalance) {
        log.debug("Using Insufficient Funds check (Max Asset Position Size Percent not set or <= 0)");
        Optional<BigDecimal> orderSizePercentDecimalOpt = settings.getOrderSizePercentDecimal();
        if (orderSizePercentDecimalOpt.isEmpty()) {
            log.error("Order Size Percent is not configured or invalid. Cannot perform insufficient funds check.");
            return createResultMapForAll(ordersToCheck, OrderStatus.FAILED, "Missing OrderSizePercent");
        }

        // Calculate total *cost* (pre-leverage) required for all new orders
        BigDecimal totalNewOrdersUsdtCost = usdtBalance.totalBalance()
                .add(usdtBalance.unPnl())
                .multiply(settings.leverage())
                .multiply(orderSizePercentDecimalOpt.get())// Based on total balance as per formula
                .multiply(BigDecimal.valueOf(ordersToCheck.size()));

        log.debug("Total estimated USDT cost for {} new orders: {} (Based on TotalBalance {} and OrderSizePercent {})",
                ordersToCheck.size(), totalNewOrdersUsdtCost, usdtBalance.totalBalance(), orderSizePercentDecimalOpt.get());


        // Compare required cost with *available* balance
        if (totalNewOrdersUsdtCost.compareTo(usdtBalance.availableBalance().multiply(settings.leverage())) > 0) {
            log.warn("Insufficient funds to place all orders. Required USDT Cost: {}, Available Balance: {}",
                    totalNewOrdersUsdtCost, usdtBalance.availableBalance());
            return createResultMapForAll(ordersToCheck, OrderStatus.INSUFFICIENT_FUNDS, "Insufficient Available Balance");
        } else {
            log.debug("Sufficient funds available. Required USDT Cost: {}, Available Balance: {}",
                    totalNewOrdersUsdtCost, usdtBalance.availableBalance());
            return Collections.emptyMap(); // All OK
        }
    }


    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Creates a result map indicating all provided orders should get the specified status.
     */
    private Map<Order, OrderStatus> createResultMapForAll(List<Order> orders, OrderStatus status, String reason) {
        log.warn("Setting status {} for all {} checked orders due to: {}", status, orders.size(), reason);
        return orders.stream()
                .filter(Objects::nonNull)
                .peek(order -> order.setStatus(status))
                .collect(Collectors.toMap(Function.identity(), order -> status));
    }

}
