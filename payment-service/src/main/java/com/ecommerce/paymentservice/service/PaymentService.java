package com.ecommerce.paymentservice.service;

import com.ecommerce.common.event.InventoryReservedEventAvro;
import com.ecommerce.common.event.PaymentFailedEventAvro;
import com.ecommerce.common.event.PaymentSuccessEventAvro;
import com.ecommerce.paymentservice.messaging.PaymentEventProducer;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.model.PaymentTransaction;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public void processPayment(InventoryReservedEventAvro event) {
        log.info("Processing payment for order {}", event.getOrderId());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            // Simulate Payment Gateway call
            boolean paymentSuccess = simulatePaymentGateway(event.getTotalAmount());

            if (paymentSuccess) {
                transaction.setStatus(PaymentStatus.SUCCESS);
                paymentTransactionRepository.save(transaction);

                PaymentSuccessEventAvro successEvent = new PaymentSuccessEventAvro(
                        event.getOrderId(),
                        transaction.getId(),
                        event.getCustomerId(),
                        event.getTotalAmount()
                );
                paymentEventProducer.sendPaymentSuccessEvent(successEvent);
            } else {
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setFailureReason("Insufficient Funds");
                paymentTransactionRepository.save(transaction);

                PaymentFailedEventAvro failedEvent = new PaymentFailedEventAvro(
                        event.getOrderId(),
                        event.getCustomerId(),
                        "Insufficient Funds",
                        event.getTotalAmount()
                );
                paymentEventProducer.sendPaymentFailedEvent(failedEvent);
            }
        } catch (Exception e) {
            log.error("Payment processing error for order {}", event.getOrderId(), e);
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            paymentTransactionRepository.save(transaction);

            PaymentFailedEventAvro failedEvent = new PaymentFailedEventAvro(
                    event.getOrderId(),
                    event.getCustomerId(),
                    e.getMessage(),
                    event.getTotalAmount()
            );
            paymentEventProducer.sendPaymentFailedEvent(failedEvent);
        }
    }

    private boolean simulatePaymentGateway(Double amount) {
        // Randomly fail 10% of the time for demonstration purposes
        return Math.random() > 0.1;
    }
}
