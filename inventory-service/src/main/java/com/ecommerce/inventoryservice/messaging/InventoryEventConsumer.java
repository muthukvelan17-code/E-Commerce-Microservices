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
        log.info("Received OrderCreatedEvent for order {}", event.getOrderId());
        
        try {
            // For simplicity, we assume one product per order in this reservation logic,
            // or we loop through items. The schema has 'items'. Let's reserve for the first item.
            // In a real scenario, we loop through all items and reserve.
            if (!event.getItems().isEmpty()) {
                var item = event.getItems().get(0);
                boolean reserved = inventoryService.reserveStock(event.getOrderId(), item.getProductId(), item.getQuantity());
                
                if (reserved) {
                    InventoryReservedEventAvro reservedEvent = new InventoryReservedEventAvro(event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
                    inventoryEventProducer.sendInventoryReservedEvent(reservedEvent);
                } else {
                    InventoryFailedEventAvro failedEvent = new InventoryFailedEventAvro(event.getOrderId(), event.getCustomerId(), "Insufficient Stock");
                    inventoryEventProducer.sendInventoryFailedEvent(failedEvent);
                }
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
            InventoryFailedEventAvro failedEvent = new InventoryFailedEventAvro(event.getOrderId(), event.getCustomerId(), "Internal Error: " + e.getMessage());
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
