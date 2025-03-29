package com.market.commander.quant.repository;

import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategySessionRepository extends JpaRepository<StrategySession, Long> {

    List<StrategySession> findByStatus(SessionStatus status);
}
