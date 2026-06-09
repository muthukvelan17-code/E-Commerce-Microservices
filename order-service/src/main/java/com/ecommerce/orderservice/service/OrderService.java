package com.ecommerce.orderservice.service;

import com.ecommerce.common.event.OrderCancelledEventAvro;
import com.ecommerce.common.event.OrderConfirmedEventAvro;
import com.ecommerce.common.event.OrderCreatedEventAvro;
import com.ecommerce.common.event.OrderItemAvro;
import com.ecommerce.orderservice.dto.OrderItemDto;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.messaging.OrderEventProducer;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String customerId) {
        log.info("Creating order for customer {}", customerId);

        // Calculate total amount
        double totalAmount = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(dto -> OrderItem.builder()
                        .productId(dto.getProductId())
                        .price(dto.getPrice())
                        .quantity(dto.getQuantity())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        // Publish OrderCreatedEvent
        List<OrderItemAvro> avroItems = items.stream()
                .map(item -> new OrderItemAvro(item.getProductId(), item.getQuantity(), item.getPrice()))
                .collect(Collectors.toList());

        OrderCreatedEventAvro event = new OrderCreatedEventAvro(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                avroItems
        );

        orderEventProducer.sendOrderCreatedEvent(event);

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void confirmOrder(String orderId) {
        log.info("Confirming order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            OrderConfirmedEventAvro event = new OrderConfirmedEventAvro(order.getId(), order.getCustomerId());
            orderEventProducer.sendOrderConfirmedEvent(event);
        }
    }

    @Transactional
    public void cancelOrder(String orderId, String reason) {
        log.info("Cancelling order {} due to: {}", orderId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            OrderCancelledEventAvro event = new OrderCancelledEventAvro(order.getId(), order.getCustomerId(), reason);
            orderEventProducer.sendOrderCancelledEvent(event);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProductId())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }
}
