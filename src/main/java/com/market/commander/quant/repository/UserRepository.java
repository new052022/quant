package com.market.commander.quant.repository;

import com.market.commander.quant.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
