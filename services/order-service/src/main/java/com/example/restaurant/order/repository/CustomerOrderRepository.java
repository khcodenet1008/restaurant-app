package com.example.restaurant.order.repository;

import com.example.restaurant.order.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
}
