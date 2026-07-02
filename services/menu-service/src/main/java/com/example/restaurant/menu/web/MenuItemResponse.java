package com.example.restaurant.menu.web;

import com.example.restaurant.menu.domain.MenuItem;
import java.math.BigDecimal;

public record MenuItemResponse(
        String id,
        String categoryId,
        String name,
        String description,
        BigDecimal priceAmount,
        String currency,
        String imageUrl,
        boolean available,
        String allergenLabels) {

    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getCategoryId(),
                item.getName(),
                item.getDescription(),
                item.getPriceAmount(),
                item.getCurrency(),
                item.getImageUrl(),
                item.isAvailable(),
                item.getAllergenLabels());
    }
}
