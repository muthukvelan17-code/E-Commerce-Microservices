package com.ecommerce.paymentservice.messaging;

import com.ecommerce.common.event.PaymentFailedEventAvro;
import com.ecommerce.common.event.PaymentSuccessEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccessEvent(PaymentSuccessEventAvro event) {
        log.info("Sending PaymentSuccessEvent for order {}", event.getOrderId());
        kafkaTemplate.send("payment-success", event.getOrderId(), event);
    }

    public void sendPaymentFailedEvent(PaymentFailedEventAvro event) {
        log.info("Sending PaymentFailedEvent for order {}", event.getOrderId());
        kafkaTemplate.send("payment-failed", event.getOrderId(), event);
    }
}
