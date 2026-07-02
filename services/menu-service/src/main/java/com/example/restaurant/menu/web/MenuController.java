package com.example.restaurant.menu.web;

import com.example.restaurant.menu.repository.MenuItemRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu/items")
public class MenuController {

    private final MenuItemRepository menuItemRepository;

    public MenuController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    public List<MenuItemResponse> getItems() {
        return menuItemRepository.findByAvailableTrueOrderByNameAsc()
                .stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getItem(@PathVariable String id) {
        return menuItemRepository.findById(id)
                .map(MenuItemResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
