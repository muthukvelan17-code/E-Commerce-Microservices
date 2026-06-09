package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, String> {
    List<StockReservation> findByOrderId(String orderId);
}
