package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.model.InventoryItem;
import com.ecommerce.inventoryservice.model.ReservationStatus;
import com.ecommerce.inventoryservice.model.StockReservation;
import com.ecommerce.inventoryservice.repository.InventoryItemRepository;
import com.ecommerce.inventoryservice.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;

    @Transactional
    public InventoryResponse addStock(InventoryRequest request) {
        log.info("Adding stock for product {}", request.getProductId());
        
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findByProductId(request.getProductId());
        InventoryItem item;
        
        if (optionalItem.isPresent()) {
            item = optionalItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = InventoryItem.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .reservedQuantity(0)
                    .build();
        }
        
        inventoryItemRepository.save(item);
        return mapToResponse(item);
    }

    public InventoryResponse getInventory(String productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        return mapToResponse(item);
    }

    @Transactional
    public boolean reserveStock(String orderId, String productId, int quantity) {
        log.info("Reserving stock for order {}, product {}, quantity {}", orderId, productId, quantity);
        
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        int availableQuantity = item.getQuantity() - item.getReservedQuantity();
        if (availableQuantity >= quantity) {
            item.setReservedQuantity(item.getReservedQuantity() + quantity);
            inventoryItemRepository.save(item);

            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .productId(productId)
                    .quantity(quantity)
                    .status(ReservationStatus.RESERVED)
                    .build();
            stockReservationRepository.save(reservation);
            
            return true;
        } else {
            log.warn("Insufficient stock for product {}", productId);
            return false;
        }
    }

    @Transactional
    public void releaseStock(String orderId) {
        log.info("Releasing stock for order {}", orderId);
        
        stockReservationRepository.findByOrderId(orderId).forEach(reservation -> {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                InventoryItem item = inventoryItemRepository.findByProductId(reservation.getProductId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + reservation.getProductId()));
                
                item.setReservedQuantity(item.getReservedQuantity() - reservation.getQuantity());
                inventoryItemRepository.save(item);
                
                reservation.setStatus(ReservationStatus.RELEASED);
                stockReservationRepository.save(reservation);
                log.info("Released {} stock for product {}", reservation.getQuantity(), reservation.getProductId());
            }
        });
    }

    @Transactional
    public void commitStock(String orderId) {
        log.info("Committing stock for order {}", orderId);
        
        stockReservationRepository.findByOrderId(orderId).forEach(reservation -> {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                InventoryItem item = inventoryItemRepository.findByProductId(reservation.getProductId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + reservation.getProductId()));
                
                item.setQuantity(item.getQuantity() - reservation.getQuantity());
                item.setReservedQuantity(item.getReservedQuantity() - reservation.getQuantity());
                inventoryItemRepository.save(item);
                
                reservation.setStatus(ReservationStatus.COMMITTED);
                stockReservationRepository.save(reservation);
                log.info("Committed {} stock for product {}", reservation.getQuantity(), reservation.getProductId());
            }
        });
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getQuantity() - item.getReservedQuantity())
                .build();
    }
}
