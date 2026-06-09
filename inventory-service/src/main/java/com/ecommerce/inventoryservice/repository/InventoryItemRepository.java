package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    Optional<InventoryItem> findByProductId(String productId);
}
