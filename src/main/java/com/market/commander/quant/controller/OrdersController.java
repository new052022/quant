package com.market.commander.quant.controller;

import com.market.commander.quant.dto.OrderRequestDto;
import com.market.commander.quant.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/strategy-execution")
public class OrdersController {

    private final OrderService orderService;

    @SneakyThrows
    @PostMapping
    public ResponseEntity<HttpStatus> executeStrategySession(@RequestBody OrderRequestDto request) {
        orderService.updateOrderResult(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
