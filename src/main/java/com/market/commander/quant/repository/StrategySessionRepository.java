package com.market.commander.quant.repository;

import com.market.commander.quant.entities.StrategySession;
import com.market.commander.quant.enums.SessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StrategySessionRepository extends JpaRepository<StrategySession, Long> {

    @EntityGraph(attributePaths = {"results", "user", "symbols", "symbols.symbol"})
    List<StrategySession> findByStatus(SessionStatus status);

    @Query("SELECT COUNT(s) > 0 FROM StrategySession s " +
            "WHERE s.exchange = :exchange AND s.user.id = :userId AND s.status = :status")
    boolean existsActiveSession(@Param("exchange") String exchange,
                                @Param("userId") Long userId,
                                @Param("status") SessionStatus status);

    StrategySession findByUser_IdAndExchangeAndStatus(@Param("userId") Long userId,
                                                      @Param("exchange") String exchange,
                                                      @Param("status") SessionStatus status);
}
