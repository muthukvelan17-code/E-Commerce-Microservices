package com.ecommerce.paymentservice.messaging;

import com.ecommerce.common.event.InventoryReservedEventAvro;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "inventory-reserved", groupId = "payment-group")
    public void consumeInventoryReservedEvent(InventoryReservedEventAvro event) {
        log.info("Received InventoryReservedEvent for order {}, initiating payment", event.getOrderId());
        paymentService.processPayment(event);
    }
}
