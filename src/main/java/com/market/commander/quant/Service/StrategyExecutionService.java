package com.market.commander.quant.Service;

import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutionService {

    private final StrategySessionService strategySessionService;

    public void prepareStrategySession(StrategySession session) {
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(now);
        session.setEndTime(now.plusHours(session.getHoursToRun()));
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void runActiveSessions() {
        log.info("Run sessions at {} ", LocalDateTime.now());
        List<StrategySession> sessions = strategySessionService.findByStatus(SessionStatus.ACTIVE);
        sessions.forEach(session -> {
            try {
                if (this.checkSessionStatus(session)) {

                }
            } catch (Exception e) {
                log.error("Failed to run session with id: {} with message: {}", session.getId(), e.getMessage());
            }
        });
    }

    private Boolean checkSessionStatus(StrategySession session) {
        return session.getEndTime().isAfter(LocalDateTime.now());
    }
}
