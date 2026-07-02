package com.example.restaurant.kitchen.web;

public record KitchenTicketItemResponse(
        String menuItemId,
        String menuItemName,
        int quantity,
        String specialInstruction) {
}
