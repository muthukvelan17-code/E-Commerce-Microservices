package com.ecommerce.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;
    
    @Indexed
    private String name;
    
    private String description;
    private Double price;
    
    @Indexed(unique = true)
    private String sku;
    
    private String categoryId;
    private Map<String, String> attributes;
}
