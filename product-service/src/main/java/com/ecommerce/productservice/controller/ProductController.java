package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing products.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product.
     *
     * @param request the product creation request
     * @return the created product response
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("REST request to create product: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the unique ID of the product
     * @return the requested product response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("REST request to get product by ID: {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Retrieves a paginated and sorted list of products, optionally filtered by keyword.
     *
     * @param page      the page index (0-based)
     * @param size      the size of the page
     * @param sortField the field to sort by
     * @param sortDir   the direction of sorting (asc/desc)
     * @param keyword   an optional search keyword
     * @return a page of product responses
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword) {
        log.info("REST request to get all products. Page: {}, Size: {}, SortField: {}, SortDir: {}, Keyword: {}",
                page, size, sortField, sortDir, keyword);
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortField, sortDir, keyword));
    }

    /**
     * Updates an existing product by its ID.
     *
     * @param id      the unique ID of the product
     * @param request the product update request
     * @return the updated product response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        log.info("REST request to update product ID: {}, SKU: {}", id, request.getSku());
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the unique ID of the product
     * @return an empty response indicating deletion success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("REST request to delete product ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
