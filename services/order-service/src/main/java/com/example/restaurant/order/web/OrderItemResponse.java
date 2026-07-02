package com.example.restaurant.order.web;

import java.math.BigDecimal;

public record OrderItemResponse(
        String menuItemId,
        String menuItemName,
        int quantity,
        BigDecimal unitPriceAmount,
        BigDecimal lineTotalAmount,
        String currency) {
}
