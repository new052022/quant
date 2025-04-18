package com.market.commander.quant.service;

import com.market.commander.quant.client.StrategyClient;
import com.market.commander.quant.dto.StopLossTakeProfitPrice;
import com.market.commander.quant.dto.StrategyParamsRequestDto;
import com.market.commander.quant.dto.StrategyResultResponseDto;
import com.market.commander.quant.dto.StrategyResultsResponseDto;
import com.market.commander.quant.dto.TpSlStrategyRequestDto;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.mapper.StrategyResultsMapper;
import com.market.commander.quant.repository.StrategyResultsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyResultsService {

    private final StrategyResultsRepository strategyResultsRepository;

    private final StrategyResultsMapper strategyResultsMapper;

    private final StrategyClient strategyClient;

    @Transactional
    public List<StrategyResult> getStrategyResults(StrategySession session) {
        StrategyParamsRequestDto request = this.buildStrategyParamsRequest(session);
        log.info("Built request to strategy service: {}", request);
        StrategyResultsResponseDto strategyResults = strategyClient.getStrategyResults(request);
        strategyResults.getOrdersList().forEach(order ->
                log.info("Get order plan from strategy service with params: {}", order));
        List<StrategyResult> results = strategyResultsMapper.toStrategyResults(strategyResults, session);
        session.getResults().addAll(results);
        return this.saveResults(results);
    }

    public List<StopLossTakeProfitPrice> getStrategyTPSLResults(StrategySession session,
                                                                Map<String, Pair<Pair<Double, Double>, Boolean>>
                                                                        positionSizesBySymbol) {
        return strategyClient.getTpSlResults(positionSizesBySymbol.entrySet().stream()
                .map(entrySet -> TpSlStrategyRequestDto.builder()
                        .volatilityCoeff(session.getVolatilityCoeff())
                        .timeframe(session.getTimeframe())
                        .period(session.getPeriod())
                        .exchange(session.getExchange())
                        .strategyType(session.getStrategyType())
                        .userId(session.getUser().getId())
                        .slowEmaPeriod(session.getSlowEmaPeriod())
                        .fastEmaPeriod(session.getFastEmaPeriod())
                        .entryPrice(entrySet.getValue().getFirst().getSecond())
                        .isUptrend(entrySet.getValue().getSecond())
                        .longAtrPeriod(session.getLongAtrPeriod())
                        .shortAtrPeriod(session.getShortAtrPeriod())
                        .symbol(entrySet.getKey())
                        .build())
                .toList());
    }

    private List<StrategyResult> saveResults(List<StrategyResult> results) {
        return strategyResultsRepository.saveAll(results);
    }

    private StrategyParamsRequestDto buildStrategyParamsRequest(StrategySession session) {
        return StrategyParamsRequestDto.builder()
                .strategyType(session.getStrategyType())
                .leverage(session.getLeverage())
                .timeframe(session.getTimeframe())
                .period(session.getPeriod())
                .entryRangeDivisor(session.getEntryRangeDivisor())
                .exchange(session.getExchange())
                .fastEmaPeriod(session.getFastEmaPeriod())
                .hitPricePercent(session.getHitPricePercent())
                .longAtrPeriod(session.getLongAtrPeriod())
                .shortAtrPeriod(session.getShortAtrPeriod())
                .slowEmaPeriod(session.getSlowEmaPeriod())
                .userId(session.getUser().getId())
                .volatilityCoeff(session.getVolatilityCoeff())
                .volume(session.getVolume())
                .build();
    }

}
