package com.market.commander.quant.service;

import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.entities.Order;
import com.market.commander.quant.util.FinancialUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class NotionalCalculator {

    public BigDecimal calculateExistingPositionsNotional(List<OpenPositionResponseDto> positions) {
        return positions.stream()
                .map(pos -> {
                    BigDecimal breakEvenPrice = FinancialUtil.safeParse(pos.getBreakEvenPrice());
                    BigDecimal positionAmt = FinancialUtil.safeParse(pos.getPositionAmt());
                    if (breakEvenPrice != null && positionAmt != null) {
                        return breakEvenPrice.multiply(positionAmt).abs();
                    }
                    log.warn("Could not parse data for existing position notional calc. Symbol: {}", pos.getSymbol());
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateOpenOrdersNotional(List<OpenOrderResponseDto> openOrders) {
        return openOrders.stream()
                .map(ord -> {
                    BigDecimal price = FinancialUtil.safeParse(ord.getPrice());
                    BigDecimal origQty = FinancialUtil.safeParse(ord.getOrigQty());
                    if (origQty != null && price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                        return origQty.multiply(price).abs();
                    } else if (origQty != null) {
                        // Fallback: Try limit price if avgPrice is 0 or invalid
                        BigDecimal limitPrice = FinancialUtil.safeParse(ord.getPrice());
                        if (limitPrice != null && limitPrice.compareTo(BigDecimal.ZERO) > 0) {
                            log.trace("Using limit price ({}) for open order {} as avgPrice is invalid/zero", limitPrice, ord.getOrderId());
                            return origQty.multiply(limitPrice).abs();
                        }
                    }
                    log.warn("Could not parse data or determine price for open order notional calc. OrderId: {}, Symbol: {}", ord.getOrderId(), ord.getSymbol());
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateNewOrdersNotional(List<Order> newOrders) {
        // This calculation remains dependent on the meaning of order.getSize() and order.getOpenPrice()
        // Assuming size = base asset quantity, openPrice = entry price proxy
        return newOrders.stream()
                .map(order -> {
                    if (order == null || order.getSize() == null || order.getParams().getMinEntryPrice() == null) {
                        log.warn("Cannot calculate notional for new order id {} due to missing data", order.getId());
                        return BigDecimal.ZERO;
                    }
                    return order.getSize();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
