package com.market.commander.quant.dto;

import com.market.commander.quant.enums.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.StringJoiner;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunStrategyRequestDto {

    private Long period;

    private String timeframe;

    private Long volume;

    private Long leverage;

    private StrategyType strategyType;

    private Double maxAtrPercent;

    private Double minAtrPercent;

    private Double orderSizePercent;

    private Double maxAssetOpenOrdersSizePercent;

    private Long userId;

    private String exchange;

    private Integer fastEmaPeriod;

    private Integer slowEmaPeriod;

    private Integer shortAtrPeriod;

    private Integer longAtrPeriod;

    /**
     * default - 2.0
     */
    private Double entryRangeDivisor;

    private Double hitPricePercent;

    private Double atrStopLossCoefficient;

    private Double atrTakeProfitCoefficient;

    private Double volatilityCoeff;

    private Long hoursToRun;

    @Override
    public String toString() {
        return new StringJoiner(", ", RunStrategyRequestDto.class.getSimpleName() + "[", "]")
                .add("period=" + period)
                .add("timeframe='" + timeframe + "'")
                .add("volume=" + volume)
                .add("leverage=" + leverage)
                .add("strategyType=" + strategyType)
                .add("maxAtrPercent=" + maxAtrPercent)
                .add("minAtrPercent=" + minAtrPercent)
                .add("orderSizePercent=" + orderSizePercent)
                .add("maxAssetOpenOrdersSizePercent=" + maxAssetOpenOrdersSizePercent)
                .add("userId=" + userId)
                .add("exchange='" + exchange + "'")
                .add("fastEmaPeriod=" + fastEmaPeriod)
                .add("slowEmaPeriod=" + slowEmaPeriod)
                .add("shortAtrPeriod=" + shortAtrPeriod)
                .add("longAtrPeriod=" + longAtrPeriod)
                .add("entryRangeDivisor=" + entryRangeDivisor)
                .add("hitPricePercent=" + hitPricePercent)
                .add("atrStopLossCoefficient=" + atrStopLossCoefficient)
                .add("atrTakeProfitCoefficient=" + atrTakeProfitCoefficient)
                .toString();
    }

}
