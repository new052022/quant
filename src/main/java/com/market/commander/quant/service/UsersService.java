package com.market.commander.quant.service;

import com.market.commander.quant.client.UsersClient;
import com.market.commander.quant.dto.UserResponseDto;
import com.market.commander.quant.entities.User;
import com.market.commander.quant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersClient usersClient;

    private final UserRepository userRepository;

    public UserResponseDto getUserDetails(Long externalId, String exchange) {
        return usersClient.getUserDetails(externalId).stream()
                .filter(details -> details.getExchangeName().equalsIgnoreCase(exchange))
                .findFirst().orElseThrow(() ->
                        new NoSuchElementException(String.format(
                                "User with id %d doesn't have data related with exchange %s", externalId, exchange)));
    }

    public Optional<User> getById(Long userId) {
        return userRepository.findById(userId);
    }

    public User create(Long userId) {
        return userRepository.save(User.builder()
                .externalId(userId)
                .build());
    }
}
