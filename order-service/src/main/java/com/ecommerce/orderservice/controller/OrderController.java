package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing customer orders.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order for the authenticated customer.
     *
     * @param customerId the unique ID of the customer
     * @param request the order details
     * @return the created order response
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody OrderRequest request) {
        log.info("REST request to create order for customer ID: {}", customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, customerId));
    }

    /**
     * Retrieves an order by its unique ID.
     *
     * @param orderId the unique ID of the order
     * @return the requested order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        log.info("REST request to get order by ID: {}", orderId);
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * Retrieves all orders for the authenticated customer.
     *
     * @param customerId the unique ID of the customer
     * @return a list of order responses
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestHeader("X-User-Id") String customerId) {
        log.info("REST request to retrieve orders for customer ID: {}", customerId);
        return ResponseEntity.ok(orderService.getOrders(customerId));
    }
}

