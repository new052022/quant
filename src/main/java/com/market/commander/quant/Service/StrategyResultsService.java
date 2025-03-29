package com.market.commander.quant.Service;

import com.market.commander.quant.client.StrategyClient;
import com.market.commander.quant.dto.StrategyParamsRequestDto;
import com.market.commander.quant.dto.StrategyResultsResponseDto;
import com.market.commander.quant.entities.StrategyResult;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.mapper.StrategyResultsMapper;
import com.market.commander.quant.repository.StrategyResultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyResultsService {

    private final StrategyResultsRepository strategyResultsRepository;

    private final StrategyResultsMapper strategyResultsMapper;

    private final StrategyClient strategyClient;

    @Transactional
    public List<StrategyResult> getStrategyResults(StrategySession session) {
        StrategyParamsRequestDto request = this.buildStrategyParamsRequest(session);
        StrategyResultsResponseDto strategyResults = strategyClient.getStrategyResults(request);
        List<StrategyResult> results = strategyResultsMapper.toStrategyResults(strategyResults, session);
        session.getResults().addAll(results);
        return this.saveResults(results);
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
