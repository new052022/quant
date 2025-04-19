package com.market.commander.quant.service;

import com.market.commander.quant.client.OrdersClient;
import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.CloseOrdersRequestDto;
import com.market.commander.quant.dto.CreateOrderRequestDto;
import com.market.commander.quant.dto.GetAssetsDataRequestDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.dto.OrderRequestDto;
import com.market.commander.quant.dto.StopLossTakeProfitPrice;
import com.market.commander.quant.dto.SymbolOrderDto;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.BinanceOrderType;
import com.market.commander.quant.enums.OrderStatus;
import com.market.commander.quant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
        Long userExternalId = session.getUser().getExternalId();
        String exchange = session.getExchange();
        UserResponseDto userDetails = usersService.getUserDetails(userExternalId, exchange);
        orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.NEW)
                .forEach(order -> {
                    try {
                        CreateOrderRequestDto request = this.buildOrderRequest(order, exchange, userDetails);
                        ordersClient.openOrder(request);
                        order.setStatus(OrderStatus.OPEN);
                    } catch (Exception e) {
                        log.error("Error during opening the order with id {} with message: {}", order.getId(), e.getMessage());
                        order.setStatus(OrderStatus.FAILED);
                    }
                    orderRepository.saveAll(orders);
                });

    }

    public List<OpenOrderResponseDto> getOpenOrders(UserResponseDto userDetails) {
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

    public void updateOrderResult(OrderRequestDto request) {
        Order order = this.findById(Long.valueOf(request.getOrderId()));
        BigDecimal newFilledAmount = order.getFilledAmount().add(BigDecimal.valueOf(request.getOrderAmount()));
        order.setFilledAmount(newFilledAmount);
        BigDecimal size = order.getSize();
        if (size.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Order size cannot be zero");
        }
        BigDecimal fillPercentage = newFilledAmount.divide(size, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (fillPercentage.compareTo(BigDecimal.valueOf(95)) >= 0) {
            order.setStatus(OrderStatus.COMPLETED);
        }
        orderRepository.save(order);
        log.info("Order updated: ID={}, Filled Amount={}, Fill Percentage={}%{}",
                order.getId(), newFilledAmount, fillPercentage,
                fillPercentage.compareTo(BigDecimal.valueOf(95)) >= 0 ? ", Status=COMPLETED" : "");
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException(String.format("Order with id %d doesn't exist", id)));
    }

    public void closeOrders(Map<String, String> ordersIdToClose, StrategySession session) {
        UserResponseDto userDetails = usersService.getUserDetails(session.getUser().getExternalId(), session.getExchange());
        ordersClient.deleteOrders(CloseOrdersRequestDto.builder()
                .apiKey(userDetails.getApiKey())
                .privateKey(userDetails.getSecretKey())
                .exchange(session.getExchange())
                .origClientOrderIdList(ordersIdToClose.entrySet().stream()
                        .map(entry -> SymbolOrderDto.builder()
                                .orderId(entry.getValue())
                                .symbol(entry.getKey())
                                .build())
                        .toList())
                .build());
    }

    public void createTPSLOrders(List<StopLossTakeProfitPrice> tpslPrices,
                                 Map<String, Pair<Pair<Double, Double>, Boolean>> symbolsData, UserResponseDto userDetails) {
        tpslPrices.forEach(tpslOrderData -> {
            try {
                String symbol = tpslOrderData.getSymbol();
                ordersClient.openOrder(this.buildTPSLOrder(symbol,
                        symbolsData.get(symbol), userDetails, tpslOrderData.getStopLoss()));
                ordersClient.openOrder(this.buildTPSLOrder(symbol,
                        symbolsData.get(symbol), userDetails, tpslOrderData.getTakeProfit()));
            } catch (Exception e) {
                log.error("Failed to create TPSL order for data: {}", tpslOrderData);
            }
        });
    }

    private CreateOrderRequestDto buildTPSLOrder(String symbol, Pair<Pair<Double, Double>, Boolean> symbolData,
                                                 UserResponseDto userDetails, Double orderPrice) {
        String side = this.defineOrderSide(symbolData.getSecond());
        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setExchange(userDetails.getExchangeName());
        request.setSymbol(symbol);
        request.setStopPrice(orderPrice.toString());
        request.setQuantity(symbolData.getFirst().getFirst().toString());
        request.setSide(side);
        request.setPrice(orderPrice.toString());
        request.setTimeInForce("GTC");
        request.setApiKey(userDetails.getApiKey());
        request.setPrivateKey(userDetails.getSecretKey());
        request.setType(this.defineOrderType(symbolData,orderPrice));
        return request;
    }

    private String defineOrderType(Pair<Pair<Double, Double>, Boolean> symbolData, Double orderPrice) {
        Boolean isPositionLong = symbolData.getSecond(); // true для длинной позиции, false для короткой
        Double openPositionPrice = symbolData.getFirst().getSecond(); // Цена открытия позиции
        if (isPositionLong) {
            return orderPrice < openPositionPrice ? BinanceOrderType.STOP.name() : BinanceOrderType.TAKE_PROFIT.name();
        } else {
            // Для короткой позиции
            return orderPrice > openPositionPrice ? BinanceOrderType.STOP.name() : BinanceOrderType.TAKE_PROFIT.name();
        }
    }

    private String defineOrderSide(Boolean isLongPosition) {
        return isLongPosition ? "SELL" : "BUY";
    }

    private CreateOrderRequestDto buildOrderRequest(Order order, String exchange, UserResponseDto userDetails) {
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
