package com.market.commander.quant.Service;

import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.SessionStatus;
import com.market.commander.quant.mapper.StrategySessionMapper;
import com.market.commander.quant.repository.StrategySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategySessionService {

    private final StrategySessionRepository strategySessionRepository;

    private final StrategyExecutionService strategyExecutionService;

    private final StrategySessionMapper strategySessionMapper;

    public RunSessionResponseDto runStrategySession(RunStrategyRequestDto inputs) {
        StrategySession strategySession = strategySessionMapper.toStrategySession(inputs);
        strategyExecutionService.prepareStrategySession(strategySession);
        StrategySession savedSession = this.save(strategySession);
        return strategySessionMapper.toRunSessionResponse(savedSession);
    }

    public StrategySession save(StrategySession session) {
        return strategySessionRepository.save(session);
    }

    public List<StrategySession> findByStatus(SessionStatus status) {
        return strategySessionRepository.findByStatus(status);
    }
}
