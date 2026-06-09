package com.ecommerce.productservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String sku;
    private String categoryId;
    private Map<String, String> attributes;
}
