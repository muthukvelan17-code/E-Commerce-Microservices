package com.ecommerce.inventoryservice.messaging;

import com.ecommerce.common.event.InventoryFailedEventAvro;
import com.ecommerce.common.event.InventoryReservedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInventoryReservedEvent(InventoryReservedEventAvro event) {
        log.info("Sending InventoryReservedEvent for order {}", event.getOrderId());
        kafkaTemplate.send("inventory-reserved", event.getOrderId(), event);
    }

    public void sendInventoryFailedEvent(InventoryFailedEventAvro event) {
        log.info("Sending InventoryFailedEvent for order {}", event.getOrderId());
        kafkaTemplate.send("inventory-failed", event.getOrderId(), event);
    }
}
