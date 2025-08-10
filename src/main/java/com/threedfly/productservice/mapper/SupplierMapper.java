package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.SupplierRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection;
import com.threedfly.productservice.repository.projection.ClosetSupplierProjection;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public SupplierMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        SupplierResponse response = modelMapper.map(supplier, SupplierResponse.class);
        // Custom logic for stock count since it's not a direct field mapping
        response.setStockCount(supplier.getStock() != null ? supplier.getStock().size() : 0);
        
        return response;
    }

    public Supplier toEntity(SupplierRequest request) {
        if (request == null) {
            return null;
        }

        return modelMapper.map(request, Supplier.class);
    }

    public void updateEntityFromRequest(Supplier supplier, SupplierRequest request) {
        if (supplier == null || request == null) {
            return;
        }

        modelMapper.map(request, supplier);
    }

    /**
     * Converts a SupplierWithDistanceProjection to a Supplier entity.
     * This method provides a clean way to convert projection results to entities
     * for use with existing mapper methods.
     */
    public Supplier fromProjection(SupplierWithDistanceProjection projection) {
        if (projection == null) {
            return null;
        }

        return modelMapper.map(projection, Supplier.class);
    }

    /**
     * Converts a SupplierWithStockProjection to a Supplier entity.
     * This method provides a clean way to convert optimized projection results to entities.
     */
    public Supplier fromStockProjection(ClosetSupplierProjection projection) {
        if (projection == null) {
            return null;
        }

        return modelMapper.map(projection, Supplier.class);
    }
}