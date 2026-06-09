package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.model.PaymentTransaction;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> getAllPayments() {
        return ResponseEntity.ok(paymentTransactionRepository.findAll());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentTransaction> getPaymentByOrderId(@PathVariable String orderId) {
        return paymentTransactionRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
