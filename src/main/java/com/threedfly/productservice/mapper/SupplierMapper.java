package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.SupplierRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection;
import com.threedfly.productservice.repository.projection.ClosetSupplierProjection;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        return SupplierResponse.builder()
                .id(supplier.getId())
                .userId(supplier.getUserId())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .state(supplier.getState())
                .country(supplier.getCountry())
                .postalCode(supplier.getPostalCode())
                .latitude(supplier.getLatitude())
                .longitude(supplier.getLongitude())
                .businessLicense(supplier.getBusinessLicense())
                .description(supplier.getDescription())
                .verified(supplier.isVerified())
                .active(supplier.isActive())
                .stockCount(supplier.getStock() != null ? supplier.getStock().size() : 0)
                .build();
    }

    public Supplier toEntity(SupplierRequest request) {
        if (request == null) {
            return null;
        }

        return Supplier.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .businessLicense(request.getBusinessLicense())
                .description(request.getDescription())
                .verified(request.isVerified())
                .active(request.isActive())
                .build();
    }

    public void updateEntityFromRequest(Supplier supplier, SupplierRequest request) {
        if (supplier == null || request == null) {
            return;
        }

        supplier.setUserId(request.getUserId());
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setCountry(request.getCountry());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setLatitude(request.getLatitude());
        supplier.setLongitude(request.getLongitude());
        supplier.setBusinessLicense(request.getBusinessLicense());
        supplier.setDescription(request.getDescription());
        supplier.setVerified(request.isVerified());
        supplier.setActive(request.isActive());
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

        return Supplier.builder()
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
    }

    /**
     * Converts a SupplierWithStockProjection to a Supplier entity.
     * This method provides a clean way to convert optimized projection results to entities.
     */
    public Supplier fromStockProjection(ClosetSupplierProjection projection) {
        if (projection == null) {
            return null;
        }

        return Supplier.builder()
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
    }
}