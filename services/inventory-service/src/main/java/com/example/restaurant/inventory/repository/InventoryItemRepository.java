package com.example.restaurant.inventory.repository;

import com.example.restaurant.inventory.domain.InventoryItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    List<InventoryItem> findByMenuItemIdIn(Collection<String> menuItemIds);
}
