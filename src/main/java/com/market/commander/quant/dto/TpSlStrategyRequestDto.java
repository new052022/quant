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
public class TpSlStrategyRequestDto {

    private String symbol;

    private Integer shortAtrPeriod;

    private Integer longAtrPeriod;

    private Boolean isUptrend;

    private Double entryPrice;

    private Integer fastEmaPeriod;

    private Integer slowEmaPeriod;

    private Long userId;

    private StrategyType strategyType;

    private String exchange;

    private Long period;

    private String timeframe;

    private Double volatilityCoeff;

    @Override
    public String toString() {
        return "OpenPosition{" +
                "symbol='" + symbol + '\'' +
                ", shortAtrPeriod=" + shortAtrPeriod +
                ", longAtrPeriod=" + longAtrPeriod +
                ", isUptrend=" + isUptrend +
                ", entryPrice=" + entryPrice +
                ", fastEmaPeriod=" + fastEmaPeriod +
                ", slowEmaPeriod=" + slowEmaPeriod +
                ", strategyType=" + strategyType.name() +
                ", userId=" + userId +
                ", exchange=" + exchange +
                ", period=" + period +
                ", timeframe=" + timeframe +
                '}';
    }

}
