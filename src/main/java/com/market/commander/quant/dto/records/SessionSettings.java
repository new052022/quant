package com.market.commander.quant.dto.records;

import com.market.commander.quant.entities.StrategySession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public record SessionSettings(Double maxAssetPositionsSizePercent, BigDecimal leverage, Double orderSizePercent) {

    private static final int DEFAULT_SCALE = 8;
    // Example scale for financial calcs
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    public SessionSettings(StrategySession session) {
        this(
                session.getMaxAssetOpenOrdersSizePercent(),
                Optional.ofNullable(session.getLeverage())
                        .filter(l -> l > 0)
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ONE), // Default leverage 1
                session.getOrderSizePercent()
        );
    }

    public boolean usePercentLimit() {
        return maxAssetPositionsSizePercent != null && maxAssetPositionsSizePercent > 0;
    }

    public Optional<BigDecimal> getMaxPercentDecimal() {
        if (!usePercentLimit()) return Optional.empty();
        return Optional.of(BigDecimal.valueOf(maxAssetPositionsSizePercent)
                .divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING));
    }

    public Optional<BigDecimal> getOrderSizePercentDecimal() {
        if (orderSizePercent == null || orderSizePercent <= 0) return Optional.empty();
        return Optional.of(BigDecimal.valueOf(orderSizePercent)
                .divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING));
    }
}
