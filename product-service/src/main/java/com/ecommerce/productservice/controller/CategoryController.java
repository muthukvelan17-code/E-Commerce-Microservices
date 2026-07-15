package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.CategoryRequest;
import com.ecommerce.productservice.model.Category;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing categories.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final ProductService productService;

    /**
     * Creates a new product category.
     *
     * @param request the category creation request
     * @return the created category response
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("REST request to create category: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(request));
    }

    /**
     * Retrieves all product categories.
     *
     * @return a list of all categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        log.info("REST request to retrieve all categories");
        return ResponseEntity.ok(productService.getAllCategories());
    }
}
