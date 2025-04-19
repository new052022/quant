package com.market.commander.quant.entities;

import com.market.commander.quant.enums.SessionStatus;
import com.market.commander.quant.enums.StrategyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "strategy_session")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StrategySession extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period")
    private Long period;

    @Column(name = "timeframe")
    private String timeframe;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "leverage")
    private Long leverage;

    @Column(name = "strategy_type")
    @Enumerated(value = EnumType.STRING)
    private StrategyType strategyType;

    @Column(name = "order_size_percent")
    private Double orderSizePercent;

    @Column(name = "max_asset_positions_size_percent")
    private Double maxAssetOpenOrdersSizePercent;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "fast_ema_period")
    private Integer fastEmaPeriod;

    @Column(name = "slow_ema_period")
    private Integer slowEmaPeriod;

    @Column(name = "short_atr_period")
    private Integer shortAtrPeriod;

    @Column(name = "long_atr_period")
    private Integer longAtrPeriod;

    @Column(name = "entry_range_divisor")
    private Double entryRangeDivisor;

    @Column(name = "hit_price_percent")
    private Double hitPricePercent;

    @Column(name = "volatility_coefficient")
    private Double volatilityCoeff;

    @Column(name = "hours_to_run")
    private Long hoursToRun;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private SessionStatus status;

    @Column(name = "start_time", updatable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "session")
    private Set<StrategyResult> results;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "strategySession")
    private Set<StrategySessionSymbol> symbols;

    @Column(name = "balance_session_limit")
    private Double balanceSessionLimit;

    @Column(name = "is_stream_connection_opened")
    private Boolean isStreamConnectionOpened;

}
