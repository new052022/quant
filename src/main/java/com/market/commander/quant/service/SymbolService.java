package com.market.commander.quant.service;

import com.market.commander.quant.entities.Symbol;
import com.market.commander.quant.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SymbolService {

    private final SymbolRepository symbolRepository;

    public List<Symbol> getOrCreateSymbols(List<String> symbols) {
        List<Symbol> existingSymbols = this.findExistingSymbols(symbols);
        List<Symbol> newSymbols = this.createNewSymbols(symbols, existingSymbols);
        this.saveNewSymbols(newSymbols);
        return this.combineSymbols(existingSymbols, newSymbols);
    }

    private List<Symbol> findExistingSymbols(List<String> symbols) {
        return symbolRepository.findByNameIn(symbols);
    }

    /**
     * Создает новые символы, которых нет в базе данных.
     */
    private List<Symbol> createNewSymbols(List<String> symbols, List<Symbol> existingSymbols) {
        Set<String> existingNames = this.extractSymbolNames(existingSymbols);
        return symbols.stream()
                .filter(name -> !existingNames.contains(name))
                .map(this::createSymbol)
                .collect(Collectors.toList());
    }

    /**
     * Извлекает имена символов из списка.
     */
    private Set<String> extractSymbolNames(List<Symbol> symbols) {
        return symbols.stream()
                .map(Symbol::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Создает новый объект Symbol.
     */
    private Symbol createSymbol(String name) {
        return Symbol.builder()
                .name(name)
                .build();
    }

    /**
     * Сохраняет новые символы в базу данных.
     */
    private void saveNewSymbols(List<Symbol> newSymbols) {
        if (!newSymbols.isEmpty()) {
            symbolRepository.saveAll(newSymbols);
        }
    }

    /**
     * Объединяет существующие и новые символы.
     */
    private List<Symbol> combineSymbols(List<Symbol> existingSymbols, List<Symbol> newSymbols) {
        return Stream.concat(existingSymbols.stream(), newSymbols.stream())
                .collect(Collectors.toList());
    }
}
