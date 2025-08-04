package com.threedfly.productservice.repository.projection;

import com.threedfly.productservice.entity.FilamentType;

/**
 * Projection interface for supplier with stock information and distance.
 * Used for optimized queries that fetch both supplier and stock data in a single query.
 */
public interface ClosetSupplierProjection {
    // Supplier fields
    Long getId();
    Long getUserId();
    String getName();
    String getEmail();
    String getPhone();
    String getAddress();
    String getCity();
    String getState();
    String getCountry();
    String getPostalCode();
    Double getLatitude();
    Double getLongitude();
    String getBusinessLicense();
    String getDescription();
    Boolean getVerified();
    Boolean getActive();
    
    // Stock fields
    Long getStockId();
    FilamentType getMaterialType();
    String getColor();
    Double getQuantityKg();
    Double getReservedKg();
    Double getAvailableQuantityKg();
    Boolean getAvailable();
    
    // Distance field
    Double getDistanceKm();
}