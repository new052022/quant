package com.market.commander.quant.controller;

import com.market.commander.quant.service.StrategySessionService;
import com.market.commander.quant.dto.RunSessionResponseDto;
import com.market.commander.quant.dto.RunStrategyRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
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

}
