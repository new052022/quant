package com.market.commander.quant.dto;

import com.market.commander.quant.enums.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyParamsRequestDto {

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

    private Double entryRangeDivisor;

    private Double hitPricePercent;

    private Double atrStopLossCoefficient;

    private Double atrTakeProfitCoefficient;

    private Double volatilityCoeff;

}
