package com.market.commander.quant.client;

import com.market.commander.quant.dto.UserRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "usersClient", url = "${users-service.url}")
public interface UsersClient {

    @GetMapping("${users-service.user-exchange-endpoint}/{userId}")
    UserResponseDto getUserDetails(UserRequestDto request);
}
