package com.market.commander.quant.service;

import com.market.commander.quant.client.UsersClient;
import com.market.commander.quant.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersClient usersClient;

    public UserResponseDto getUserDetails(Long externalId, String exchange) {
        return usersClient.getUserDetails(externalId).stream()
                .filter(details -> details.getExchangeName().equalsIgnoreCase(exchange))
                .findFirst().orElseThrow(() ->
                        new NoSuchElementException(String.format(
                                "User with id %d doesn't have data related with exchange %s", externalId, exchange)));
    }
}
