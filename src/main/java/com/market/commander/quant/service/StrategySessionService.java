package com.market.commander.quant.service;

import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import com.market.commander.quant.dto.StopSessionRequestDto;
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
        this.checkActiveSessions(inputs.getExchange(), inputs.getUserId());
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

    @Transactional
    public void stopSession(StopSessionRequestDto request) {
        StrategySession session = strategySessionRepository.findByUser_IdAndExchangeAndStatus(
                request.getUserId(), request.getExchange(), SessionStatus.ACTIVE);
        session.setStatus(SessionStatus.INACTIVE);
        session.setEndTime(LocalDateTime.now());
        strategySessionRepository.save(session);
    }

    private void checkActiveSessions(String exchange, Long userId) {
        boolean hasActiveSession = strategySessionRepository.existsActiveSession(exchange, userId, SessionStatus.ACTIVE);
        if (hasActiveSession) {
            throw new IllegalStateException("Active session already exists for exchange: " + exchange + " and user ID: " + userId);
        }
    }

    public void saveAll(List<StrategySession> sessionsToSave) {
        strategySessionRepository.saveAll(sessionsToSave);
    }
}
