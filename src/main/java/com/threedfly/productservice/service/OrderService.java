package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ClosestSupplierResponse;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.dto.OrderRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.FilamentStockMapper;
import com.threedfly.productservice.mapper.SupplierMapper;
import com.threedfly.productservice.repository.FilamentStockRepository;
import com.threedfly.productservice.repository.SupplierRepository;
import com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection;
import com.threedfly.productservice.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    
    private final SupplierRepository supplierRepository;
    private final FilamentStockRepository filamentStockRepository;
    private final SupplierMapper supplierMapper;
    private final FilamentStockMapper filamentStockMapper;
    
    /**
     * Find the closest supplier that has the required filament stock available.
     * 
     * OPTIMIZED Algorithm for Large Datasets:
     * 1. Single database query with spatial calculations and JOIN on stock
     * 2. Distance calculated directly in SQL using Haversine formula
     * 3. Filters by active, verified, coordinates, and stock availability
     * 4. Orders by distance and limits results for performance
     * 5. Returns the closest supplier efficiently
     * 
     * Performance: O(log n) with proper indexing vs O(n) with the naive approach
     * 
     * @param orderRequest The order details including buyer location and requirements
     * @return ClosestSupplierResponse with the best supplier (always successful)
     * @throws RuntimeException if no supplier is found with sufficient stock
     */
    public ClosestSupplierResponse findClosestSupplier(OrderRequest orderRequest) {
        log.info("Finding closest supplier for material: {}, color: {}, quantity: {} kg, buyer location: ({}, {})",
                orderRequest.getMaterialType(), orderRequest.getColor(), orderRequest.getRequiredQuantityKg(),
                orderRequest.getBuyerLatitude(), orderRequest.getBuyerLongitude());
        
        // Single optimized database query that finds the closest supplier directly
        Optional<SupplierWithDistanceProjection> supplierProjection =
                supplierRepository.findClosestSupplierWithStock(
                        orderRequest.getBuyerLatitude(),
                        orderRequest.getBuyerLongitude(),
                        orderRequest.getMaterialType().name(), // Convert enum to string
                        orderRequest.getColor(),
                        orderRequest.getRequiredQuantityKg()
                );
        
        if (supplierProjection.isEmpty()) {
            String errorMessage = String.format("No supplier found with sufficient stock for %s %s (required: %.1f kg)", 
                    orderRequest.getMaterialType(), orderRequest.getColor(), orderRequest.getRequiredQuantityKg());
            log.warn("No supplier found - Material: {}, Color: {}, Quantity: {} kg", 
                    orderRequest.getMaterialType(), orderRequest.getColor(), orderRequest.getRequiredQuantityKg());
            throw new RuntimeException(errorMessage);
        }
        
        // Convert projection to Supplier entity for mapper compatibility
        var projection = supplierProjection.get();
        Supplier closestSupplier = Supplier.builder()
                .id(projection.getId())
                .userId(projection.getUserId())
                .name(projection.getName())
                .email(projection.getEmail())
                .phone(projection.getPhone())
                .address(projection.getAddress())
                .city(projection.getCity())
                .state(projection.getState())
                .country(projection.getCountry())
                .postalCode(projection.getPostalCode())
                .latitude(projection.getLatitude())
                .longitude(projection.getLongitude())
                .businessLicense(projection.getBusinessLicense())
                .description(projection.getDescription())
                .verified(projection.getVerified())
                .active(projection.getActive())
                .build();
        
        // Get the stock information (we know it exists from the JOIN)
        Optional<FilamentStock> stockOptional = findBestAvailableStock(
                closestSupplier.getId(), 
                orderRequest.getMaterialType(), 
                orderRequest.getColor(), 
                orderRequest.getRequiredQuantityKg()
        );
        
        if (stockOptional.isEmpty()) {
            // This should not happen due to the JOIN, but handle gracefully
            String errorMessage = String.format("Stock information unavailable for supplier %d - data consistency issue", 
                    closestSupplier.getId());
            log.error("Stock not found for supplier {} despite JOIN query - this indicates a data consistency issue", 
                    closestSupplier.getId());
            throw new RuntimeException(errorMessage);
        }
        
        SupplierResponse supplierResponse = supplierMapper.toResponse(closestSupplier);
        FilamentStockResponse stockResponse = filamentStockMapper.toResponse(stockOptional.get());
        double roundedDistance = DistanceCalculator.roundDistance(projection.getDistanceKm(), 2);
        
        log.info("Closest supplier found: {} ({} km away) with {} kg available stock",
                closestSupplier.getName(), roundedDistance, stockOptional.get().getAvailableQuantityKg());
        
        return ClosestSupplierResponse.success(supplierResponse, stockResponse, roundedDistance);
    }

    /**
     * Find the best available stock for a supplier that meets the requirements.
     * Prefers stock with more available quantity to ensure better availability.
     * 
     * @param supplierId The supplier ID
     * @param materialType Required material type
     * @param color Required color
     * @param requiredQuantity Required quantity in kg
     * @return Optional of the best matching stock or empty if none available
     */
    private Optional<FilamentStock> findBestAvailableStock(Long supplierId, 
                                                          com.threedfly.productservice.entity.FilamentType materialType, 
                                                          String color, 
                                                          Double requiredQuantity) {
        
        List<FilamentStock> availableStocks = filamentStockRepository
                .findBestAvailableStockForSupplier(supplierId, materialType, color, requiredQuantity);
        
        return availableStocks.stream().findFirst(); // Repository query already orders by best match
    }
}