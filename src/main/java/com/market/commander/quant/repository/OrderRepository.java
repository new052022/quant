package com.market.commander.quant.repository;

import com.market.commander.quant.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
