package com.market.commander.quant.dto.records;

import com.market.commander.quant.dto.AccountBalanceDto;
import com.market.commander.quant.dto.OpenOrderResponseDto;
import com.market.commander.quant.dto.OpenPositionResponseDto;
import com.market.commander.quant.service.MoneyManagementService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

public record TradingContext(List<AccountBalanceDto> balances,
                             List<OpenOrderResponseDto> openOrders,
                             List<OpenPositionResponseDto> openPositions) {

    private static final String USDT_ASSET = "USDT";

    public Optional<BalanceInfo> getUsdtBalanceInfo() {
        return balances.stream()
                .filter(b -> USDT_ASSET.equalsIgnoreCase(b.getAsset()))
                .findFirst()
                .flatMap(BalanceInfo::fromDto); // Use flatMap to handle parsing failure
    }

    public Map<String, List<OpenOrderResponseDto>> getOpenOrdersBySymbol() {
        return openOrders.stream()
                .filter(o -> o != null && o.getSymbol() != null)
                .collect(groupingBy(OpenOrderResponseDto::getSymbol));
    }

    public Map<String, List<OpenPositionResponseDto>> getOpenPositionsBySymbol() {
        return openPositions.stream()
                .filter(p -> p != null && p.getSymbol() != null)
                .collect(groupingBy(OpenPositionResponseDto::getSymbol));
    }
}
