package com.market.commander.quant.repository;

import com.market.commander.quant.entities.StrategyResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyResultsRepository extends JpaRepository<StrategyResult, Long> {
}
