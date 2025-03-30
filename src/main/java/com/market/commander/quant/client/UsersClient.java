package com.market.commander.quant.client;

import com.market.commander.quant.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "usersClient", url = "${users-service.url}")
public interface UsersClient {

    @GetMapping("/user-exchange-info/{userId}")
    List<UserResponseDto> getUserDetails(@PathVariable("userId") Long userId);

}
