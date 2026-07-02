package com.example.restaurant.kitchen.repository;

import com.example.restaurant.kitchen.domain.KitchenTicket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, String> {

    List<KitchenTicket> findByStatusOrderByCreatedAtAsc(String status);

    List<KitchenTicket> findAllByOrderByCreatedAtAsc();

    Optional<KitchenTicket> findByOrderId(String orderId);
}
