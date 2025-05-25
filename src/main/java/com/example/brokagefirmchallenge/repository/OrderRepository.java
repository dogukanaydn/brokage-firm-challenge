package com.example.brokagefirmchallenge.repository;

import com.example.brokagefirmchallenge.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime from, LocalDateTime to);
}
