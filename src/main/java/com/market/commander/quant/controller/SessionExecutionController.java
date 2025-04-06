package com.market.commander.quant.controller;

import com.market.commander.quant.service.StrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/strategy-execution")
public class SessionExecutionController {

    private final StrategyExecutionService strategyExecutionService;

    @SneakyThrows
    @PutMapping
    public ResponseEntity<HttpStatus> runSessions() {
        strategyExecutionService.runActiveSessions();
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
