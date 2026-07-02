package com.example.restaurant.menu.repository;

import com.example.restaurant.menu.domain.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    List<MenuItem> findByAvailableTrueOrderByNameAsc();
}
