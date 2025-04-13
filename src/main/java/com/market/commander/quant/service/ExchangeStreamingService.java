package com.market.commander.quant.service;

import com.market.commander.quant.client.ExchangeStreamingClient;
import com.market.commander.quant.dto.OpenWebsocketChannelRequest;
import com.market.commander.quant.dto.WebsocketChannelResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeStreamingService {

    private final ExchangeStreamingClient exchangeStreamingClient;

    public WebsocketChannelResultResponse openWebsocketConnection(Long userId, Long strategySessionId, String exchange) {
        return exchangeStreamingClient.runWebsocketChannel(OpenWebsocketChannelRequest.builder()
                .userId(userId)
                .strategySessionId(strategySessionId)
                .exchange(exchange)
                .build());
    }

}
