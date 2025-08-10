package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ClosestSupplierResponse;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.dto.GeocodingResponse;
import com.threedfly.productservice.dto.OrderRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.exception.StockDataInconsistencyException;
import com.threedfly.productservice.exception.SupplierNotFoundException;
import com.threedfly.productservice.mapper.FilamentStockMapper;
import com.threedfly.productservice.mapper.SupplierMapper;

import com.threedfly.productservice.repository.SupplierRepository;
import com.threedfly.productservice.repository.projection.ClosetSupplierProjection;
import com.threedfly.productservice.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final SupplierRepository supplierRepository;
    private final GeocodingService geocodingService;

    private final SupplierMapper supplierMapper;
    private final FilamentStockMapper filamentStockMapper;

    /**
     * Find the closest supplier that has the required filament stock available.
     * <p>
     * OPTIMIZED Algorithm for Large Datasets:
     * 1. Address enrichment: If coordinates are missing, geocode the address
     * 2. Single database query with spatial calculations and JOIN on stock
     * 3. Distance calculated directly in SQL using Haversine formula
     * 4. Filters by active, verified, coordinates, and stock availability
     * 5. Orders by distance and limits results for performance
     * 6. Returns the closest supplier efficiently
     * <p>
     * Performance: O(log n) with proper indexing vs O(n) with the naive approach
     *
     * @param orderRequest The order details including buyer location and requirements
     * @return ClosestSupplierResponse with the best supplier (always successful)
     * @throws SupplierNotFoundException       if no supplier is found with sufficient stock
     * @throws StockDataInconsistencyException if stock data integrity issues occur
     * @throws IllegalArgumentException        if address geocoding fails and coordinates are missing
     */
    public ClosestSupplierResponse findClosestSupplier(OrderRequest orderRequest) {
        // Enrich coordinates from address if they are missing
        enrichCoordinatesIfNeeded(orderRequest);
        
        log.info("Finding closest supplier for material: {}, color: {}, quantity: {} kg, buyer location: ({}, {})",
                orderRequest.getMaterialType(), orderRequest.getColor(), orderRequest.getRequiredQuantityKg(),
                orderRequest.getBuyerLatitude(), orderRequest.getBuyerLongitude());

        var closetSupplierProjection = getClosetSupplierWithStockProjection(orderRequest);
        var closestSupplier = supplierMapper.fromStockProjection(closetSupplierProjection);
        var availableStock = filamentStockMapper.fromStockProjection(closetSupplierProjection, closestSupplier);

        SupplierResponse supplierResponse = supplierMapper.toResponse(closestSupplier);
        FilamentStockResponse stockResponse = filamentStockMapper.toResponse(availableStock);
        double roundedDistance = DistanceCalculator.roundDistance(closetSupplierProjection.getDistanceKm(), 2);

        log.info("Closest supplier found: {} ({} km away) with {} kg available stock",
                closestSupplier.getName(), roundedDistance, availableStock.getAvailableQuantityKg());

        return ClosestSupplierResponse.success(supplierResponse, stockResponse, roundedDistance);
    }

    private ClosetSupplierProjection getClosetSupplierWithStockProjection(OrderRequest orderRequest) {
        // Single optimized database query that finds the closest supplier and stock in one go
        Optional<ClosetSupplierProjection> supplierStockProjection =
                supplierRepository.findClosestSupplierWithStock(
                        orderRequest.getBuyerLatitude(),
                        orderRequest.getBuyerLongitude(),
                        orderRequest.getMaterialType().name(), // Convert enum to string
                        orderRequest.getColor(),
                        orderRequest.getRequiredQuantityKg()
                );

        if (supplierStockProjection.isEmpty()) {
            log.warn("No supplier found - Material: {}, Color: {}, Quantity: {} kg",
                    orderRequest.getMaterialType(), orderRequest.getColor(), orderRequest.getRequiredQuantityKg());
            throw SupplierNotFoundException.forMaterialRequirement(
                    orderRequest.getMaterialType().name(),
                    orderRequest.getColor(),
                    orderRequest.getRequiredQuantityKg()
            );
        }

        // Convert projection to entities using clean mapper methods
        return supplierStockProjection.get();
    }

    /**
     * Enriches the order request with coordinates if they are missing.
     * Uses the geocoding service to convert the buyer address to coordinates.
     * Modifies the orderRequest object in-place.
     * 
     * @param orderRequest The order request to enrich (modified in-place)
     * @throws IllegalArgumentException if geocoding fails and coordinates are missing
     */
    private void enrichCoordinatesIfNeeded(OrderRequest orderRequest) {
        // If coordinates are already provided, no enrichment needed
        if (!geocodingService.areCoordinatesMissing(orderRequest.getBuyerLatitude(), orderRequest.getBuyerLongitude())) {
            return;
        }

        // Attempt to geocode the address
        GeocodingResponse geocodingResponse = geocodingService.geocodeAddress(orderRequest.getBuyerAddress());
        
        if (!geocodingResponse.isSuccess()) {
            String errorMsg = String.format(
                "Failed to geocode buyer address '%s': %s. Please provide buyer coordinates manually.",
                orderRequest.getBuyerAddress(), geocodingResponse.getErrorMessage()
            );

            throw new IllegalArgumentException(errorMsg);
        }

        // Enrich the existing request with the geocoded coordinates
        orderRequest.setBuyerLatitude(geocodingResponse.getLatitude());
        orderRequest.setBuyerLongitude(geocodingResponse.getLongitude());
    }

}