package com.example.restaurant.kitchen.repository;

import com.example.restaurant.kitchen.domain.KitchenTicketItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenTicketItemRepository extends JpaRepository<KitchenTicketItem, Long> {

    List<KitchenTicketItem> findByTicketId(String ticketId);
}
