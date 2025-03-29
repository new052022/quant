package com.market.commander.quant.Service;

import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.SessionStatus;
import com.market.commander.quant.mapper.StrategySessionMapper;
import com.market.commander.quant.repository.StrategySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategySessionService {

    private final StrategySessionRepository strategySessionRepository;

    private final StrategySessionMapper strategySessionMapper;

    @Transactional
    public RunSessionResponseDto runStrategySession(RunStrategyRequestDto inputs) {
        StrategySession strategySession = strategySessionMapper.toStrategySession(inputs);
        this.prepareStrategySession(strategySession);
        StrategySession savedSession = this.save(strategySession);
        return strategySessionMapper.toRunSessionResponse(savedSession);
    }

    @Transactional
    public StrategySession save(StrategySession session) {
        return strategySessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<StrategySession> findByStatus(SessionStatus status) {
        return strategySessionRepository.findByStatus(status);
    }

    @Transactional
    public void closeSessions(List<StrategySession> sessionsToClose) {
        List<StrategySession> inactiveSessions = sessionsToClose.stream()
                .peek(session -> session.setStatus(SessionStatus.INACTIVE))
                .toList();
        strategySessionRepository.saveAll(inactiveSessions);
    }

    public void prepareStrategySession(StrategySession session) {
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(now);
        session.setEndTime(now.plusHours(session.getHoursToRun()));
    }
}
