package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing inventory.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Adds stock to inventory.
     *
     * @param request the inventory stock request
     * @return the updated inventory response
     */
    @PostMapping
    public ResponseEntity<InventoryResponse> addStock(@Valid @RequestBody InventoryRequest request) {
        log.info("REST request to add stock for product ID: {}, quantity: {}", request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addStock(request));
    }

    /**
     * Retrieves inventory details for a product by its product ID.
     *
     * @param productId the unique ID of the product
     * @return the inventory details response
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        log.info("REST request to retrieve inventory for product ID: {}", productId);
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }
}
