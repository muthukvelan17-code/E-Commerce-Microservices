package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.model.PaymentTransaction;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing payment transactions.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentTransactionRepository paymentTransactionRepository;

    /**
     * Retrieves all payment transactions.
     *
     * @return a list of all payment transactions
     */
    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> getAllPayments() {
        log.info("REST request to retrieve all payments");
        return ResponseEntity.ok(paymentTransactionRepository.findAll());
    }

    /**
     * Retrieves a payment transaction by its associated order ID.
     *
     * @param orderId the unique ID of the order
     * @return the requested payment transaction details
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentTransaction> getPaymentByOrderId(@PathVariable String orderId) {
        log.info("REST request to retrieve payment for order ID: {}", orderId);
        return paymentTransactionRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Payment transaction not found for order ID: {}", orderId);
                    return ResponseEntity.notFound().build();
                });
    }
}
