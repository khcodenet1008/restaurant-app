package com.example.restaurant.inventory.repository;

import com.example.restaurant.inventory.domain.StockReservationLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationLineRepository extends JpaRepository<StockReservationLine, Long> {

    List<StockReservationLine> findByReservationId(String reservationId);
}
