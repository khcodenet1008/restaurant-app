package com.example.restaurant.payment.web;

import com.example.restaurant.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/mock-confirm")
    public ResponseEntity<PaymentResponse> mockConfirm(@Valid @RequestBody MockPaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirm(request));
    }
}
