package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String customerId;
    private OrderStatus status;
    private Double totalAmount;
    private List<OrderItemDto> items;
}
