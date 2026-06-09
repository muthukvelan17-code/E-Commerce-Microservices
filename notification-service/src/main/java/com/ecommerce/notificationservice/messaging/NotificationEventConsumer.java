package com.ecommerce.notificationservice.messaging;

import com.ecommerce.common.event.OrderCancelledEventAvro;
import com.ecommerce.common.event.OrderConfirmedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    @KafkaListener(topics = "order-confirmed", groupId = "notification-group")
    public void consumeOrderConfirmedEvent(OrderConfirmedEventAvro event) {
        log.info("=========================================");
        log.info("Email/SMS Notification Sent to Customer ID: {}", event.getCustomerId());
        log.info("Message: Your Order {} has been successfully confirmed and payment is processed.", event.getOrderId());
        log.info("=========================================");
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notification-group")
    public void consumeOrderCancelledEvent(OrderCancelledEventAvro event) {
        log.info("=========================================");
        log.info("Email/SMS Notification Sent to Customer ID: {}", event.getCustomerId());
        log.info("Message: Your Order {} has been cancelled. Reason: {}", event.getOrderId(), event.getReason());
        log.info("=========================================");
    }
}
