package com.market.commander.quant.controller;

import com.market.commander.quant.dto.SessionSymbolsRequestDto;
import com.market.commander.quant.dto.SessionSymbolsResponseDto;
import com.market.commander.quant.dto.StopSessionRequestDto;
import com.market.commander.quant.service.StrategySessionService;
import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/strategy-session")
public class StrategySessionController {

    private final StrategySessionService strategySessionService;

    @SneakyThrows
    @PostMapping
    public ResponseEntity<RunSessionResponseDto> executeStrategySession(@RequestBody RunStrategyRequestDto inputs) {
        return ResponseEntity.ok(strategySessionService.runStrategySession(inputs));
    }

    @SneakyThrows
    @PatchMapping
    public ResponseEntity<SessionSymbolsResponseDto> executeStrategySession(@RequestBody SessionSymbolsRequestDto request) {
        return ResponseEntity.ok(strategySessionService.updateSessionSymbols(request));
    }

    @SneakyThrows
    @PostMapping("/stop")
    public ResponseEntity<HttpStatus> stopSession(@RequestBody StopSessionRequestDto request) {
        strategySessionService.stopSession(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
