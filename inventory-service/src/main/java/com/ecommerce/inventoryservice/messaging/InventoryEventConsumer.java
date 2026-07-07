package com.ecommerce.inventoryservice.messaging;

import com.ecommerce.common.event.OrderCancelledEventAvro;
import com.ecommerce.common.event.OrderCreatedEventAvro;
import com.ecommerce.common.event.PaymentFailedEventAvro;
import com.ecommerce.common.event.InventoryFailedEventAvro;
import com.ecommerce.common.event.InventoryReservedEventAvro;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void consumeOrderCreatedEvent(OrderCreatedEventAvro event) {
        log.info("Received OrderCreatedEvent for order {} with {} item(s)", event.getOrderId(), event.getItems().size());
        
        try {
            if (event.getItems().isEmpty()) {
                log.warn("Order {} has no items, skipping reservation", event.getOrderId());
                return;
            }

            // Reserve stock for ALL items in the order
            boolean allReserved = true;
            for (var item : event.getItems()) {
                boolean reserved = inventoryService.reserveStock(
                        event.getOrderId(), item.getProductId(), item.getQuantity());
                if (!reserved) {
                    allReserved = false;
                    log.warn("Failed to reserve stock for product {} in order {}", item.getProductId(), event.getOrderId());
                    break;
                }
            }

            if (allReserved) {
                log.info("All items reserved successfully for order {}", event.getOrderId());
                InventoryReservedEventAvro reservedEvent = new InventoryReservedEventAvro(
                        event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
                inventoryEventProducer.sendInventoryReservedEvent(reservedEvent);
            } else {
                // Rollback any partial reservations made for this order
                inventoryService.releaseStock(event.getOrderId());
                InventoryFailedEventAvro failedEvent = new InventoryFailedEventAvro(
                        event.getOrderId(), event.getCustomerId(), "Insufficient stock for one or more items");
                inventoryEventProducer.sendInventoryFailedEvent(failedEvent);
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for order {}", event.getOrderId(), e);
            // Rollback any partial reservations on error
            inventoryService.releaseStock(event.getOrderId());
            InventoryFailedEventAvro failedEvent = new InventoryFailedEventAvro(
                    event.getOrderId(), event.getCustomerId(), "Internal Error: " + e.getMessage());
            inventoryEventProducer.sendInventoryFailedEvent(failedEvent);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "inventory-group")
    public void consumePaymentFailedEvent(PaymentFailedEventAvro event) {
        log.info("Received PaymentFailedEvent for order {}, rolling back stock", event.getOrderId());
        inventoryService.releaseStock(event.getOrderId());
    }

    @KafkaListener(topics = "order-cancelled", groupId = "inventory-group")
    public void consumeOrderCancelledEvent(OrderCancelledEventAvro event) {
        log.info("Received OrderCancelledEvent for order {}, rolling back stock", event.getOrderId());
        inventoryService.releaseStock(event.getOrderId());
    }
}
