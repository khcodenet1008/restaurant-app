package com.example.restaurant.inventory.repository;

import com.example.restaurant.inventory.domain.StockReservation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, String> {

    Optional<StockReservation> findByOrderId(String orderId);
}
