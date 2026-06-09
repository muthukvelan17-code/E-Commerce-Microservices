package com.ecommerce.orderservice.messaging;

import com.ecommerce.common.event.InventoryFailedEventAvro;
import com.ecommerce.common.event.PaymentFailedEventAvro;
import com.ecommerce.common.event.PaymentSuccessEventAvro;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-success", groupId = "order-group")
    public void consumePaymentSuccessEvent(PaymentSuccessEventAvro event) {
        log.info("Received PaymentSuccessEvent for order {}, confirming order", event.getOrderId());
        orderService.confirmOrder(event.getOrderId());
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-group")
    public void consumePaymentFailedEvent(PaymentFailedEventAvro event) {
        log.info("Received PaymentFailedEvent for order {}, cancelling order", event.getOrderId());
        orderService.cancelOrder(event.getOrderId(), "Payment Failed: " + event.getReason());
    }

    @KafkaListener(topics = "inventory-failed", groupId = "order-group")
    public void consumeInventoryFailedEvent(InventoryFailedEventAvro event) {
        log.info("Received InventoryFailedEvent for order {}, cancelling order", event.getOrderId());
        orderService.cancelOrder(event.getOrderId(), "Inventory Failed: " + event.getReason());
    }
}
