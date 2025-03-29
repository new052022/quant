package com.market.commander.quant.repository;

import com.market.commander.quant.entities.OpenPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {
}
