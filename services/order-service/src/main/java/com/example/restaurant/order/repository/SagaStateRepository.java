package com.example.restaurant.order.repository;

import com.example.restaurant.order.domain.SagaState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStateRepository extends JpaRepository<SagaState, String> {

    Optional<SagaState> findByOrderId(String orderId);
}
