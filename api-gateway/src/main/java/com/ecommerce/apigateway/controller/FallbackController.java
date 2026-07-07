package com.ecommerce.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return buildFallbackResponse("User Service");
    }

    @RequestMapping("/product-service")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        return buildFallbackResponse("Product Service");
    }

    @RequestMapping("/inventory-service")
    public ResponseEntity<Map<String, Object>> inventoryServiceFallback() {
        return buildFallbackResponse("Inventory Service");
    }

    @RequestMapping("/order-service")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return buildFallbackResponse("Order Service");
    }

    @RequestMapping("/payment-service")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return buildFallbackResponse("Payment Service");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "error", serviceName + " is currently unavailable. Please try again later."
                ));
    }
}

