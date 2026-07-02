package com.example.restaurant.payment.repository;

import com.example.restaurant.payment.domain.PaymentAuthorization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAuthorizationRepository extends JpaRepository<PaymentAuthorization, String> {

    Optional<PaymentAuthorization> findByOrderId(String orderId);
}
