package com.ecommerce.orderservice.messaging;

import com.ecommerce.common.event.OrderCancelledEventAvro;
import com.ecommerce.common.event.OrderConfirmedEventAvro;
import com.ecommerce.common.event.OrderCreatedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEventAvro event) {
        log.info("Publishing OrderCreatedEvent for order {}", event.getOrderId());
        kafkaTemplate.send("order-created", event.getOrderId(), event);
    }

    public void sendOrderConfirmedEvent(OrderConfirmedEventAvro event) {
        log.info("Publishing OrderConfirmedEvent for order {}", event.getOrderId());
        kafkaTemplate.send("order-confirmed", event.getOrderId(), event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEventAvro event) {
        log.info("Publishing OrderCancelledEvent for order {}", event.getOrderId());
        kafkaTemplate.send("order-cancelled", event.getOrderId(), event);
    }
}
