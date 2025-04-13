package com.market.commander.quant.client;

import com.market.commander.quant.dto.OpenWebsocketChannelRequest;
import com.market.commander.quant.dto.WebsocketChannelResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "exchange-streaming", url = "${exchange-streaming.url}")
public interface ExchangeStreamingClient {

    @PostMapping("/stream-session")
    WebsocketChannelResultResponse runWebsocketChannel(OpenWebsocketChannelRequest request);
}
