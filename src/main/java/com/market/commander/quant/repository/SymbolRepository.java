package com.market.commander.quant.repository;

import com.market.commander.quant.entities.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SymbolRepository extends JpaRepository<Symbol, Long> {

    List<Symbol> findByNameIn(List<String> symbols);

}
