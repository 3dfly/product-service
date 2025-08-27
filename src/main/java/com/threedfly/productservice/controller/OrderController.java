package com.threedfly.productservice.controller;

import dto.ClosestSupplierResponse;
import dto.OrderRequest;
import service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Validated
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Find the closest supplier that has the required filament stock available for an order.
     * 
     * This endpoint uses a geographic distance calculation algorithm to find the optimal supplier:
     * 1. Filters active and verified suppliers with valid coordinates
     * 2. Checks each supplier for required filament stock availability
     * 3. Calculates distance from buyer to each qualifying supplier using Haversine formula
     * 4. Returns the closest supplier with sufficient stock
     * 
     * @param orderRequest Order details including buyer location and filament requirements
     * @return ClosestSupplierResponse containing the best supplier match or error message
     */
    @PostMapping("/find-closest-supplier")
    public ResponseEntity<ClosestSupplierResponse> findClosestSupplier(@Valid @RequestBody OrderRequest orderRequest) {
        ClosestSupplierResponse response = orderService.findClosestSupplier(orderRequest);
        
        // Always return 200 OK - the response contains either success or failure reason
        // This is a business logic result, not a technical error
        return ResponseEntity.ok(response);
    }
}