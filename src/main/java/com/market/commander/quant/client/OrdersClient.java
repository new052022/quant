package com.market.commander.quant.client;

import com.market.commander.quant.dto.OpenPositionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(name = "orders-client", url = "${orders-service.url}")
public interface OrdersClient {

    List<OpenPositionResponseDto> getOpenPositions(Long externalId);
}
