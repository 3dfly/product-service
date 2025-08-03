package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.FilamentStockRequest;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class FilamentStockMapper {

    public FilamentStockResponse toResponse(FilamentStock filamentStock) {
        if (filamentStock == null) {
            return null;
        }

        return FilamentStockResponse.builder()
                .id(filamentStock.getId())
                .supplierId(filamentStock.getSupplier() != null ? filamentStock.getSupplier().getId() : null)
                .supplierName(filamentStock.getSupplier() != null ? filamentStock.getSupplier().getName() : null)
                .materialType(filamentStock.getMaterialType())
                .color(filamentStock.getColor())
                .quantityKg(filamentStock.getQuantityKg())
                .reservedKg(filamentStock.getReservedKg())
                .available(filamentStock.isAvailable())
                .lastRestocked(filamentStock.getLastRestocked())
                .expiryDate(filamentStock.getExpiryDate())
                .availableQuantityKg(filamentStock.getAvailableQuantityKg())
                .build();
    }

    public FilamentStock toEntity(FilamentStockRequest request) {
        if (request == null) {
            return null;
        }

        FilamentStock filamentStock = FilamentStock.builder()
                .materialType(request.getMaterialType())
                .color(request.getColor())
                .quantityKg(request.getQuantityKg())
                .reservedKg(request.getReservedKg())
                .available(request.isAvailable())
                .lastRestocked(request.getLastRestocked())
                .expiryDate(request.getExpiryDate())
                .build();

        // Supplier will be set by the service layer
        return filamentStock;
    }

    public void updateEntityFromRequest(FilamentStock filamentStock, FilamentStockRequest request) {
        if (filamentStock == null || request == null) {
            return;
        }

        filamentStock.setMaterialType(request.getMaterialType());
        filamentStock.setColor(request.getColor());
        filamentStock.setQuantityKg(request.getQuantityKg());
        filamentStock.setReservedKg(request.getReservedKg());
        filamentStock.setAvailable(request.isAvailable());
        filamentStock.setLastRestocked(request.getLastRestocked());
        filamentStock.setExpiryDate(request.getExpiryDate());
        // Supplier will be set by the service layer
    }
}