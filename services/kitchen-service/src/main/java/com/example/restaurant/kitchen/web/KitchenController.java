package com.example.restaurant.kitchen.web;

import com.example.restaurant.kitchen.service.KitchenService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kitchen/tickets")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping
    public List<KitchenTicketResponse> getTickets(@RequestParam(required = false) String status) {
        return kitchenService.getTickets(status);
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<KitchenTicketResponse> updateStatus(
            @PathVariable String ticketId,
            @Valid @RequestBody UpdateKitchenTicketStatusRequest request) {
        return ResponseEntity.ok(kitchenService.updateStatus(ticketId, request));
    }
}
