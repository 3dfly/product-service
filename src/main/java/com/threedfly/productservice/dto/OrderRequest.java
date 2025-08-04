package com.threedfly.productservice.dto;

import com.threedfly.productservice.entity.FilamentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotNull(message = "Material type is required")
    private FilamentType materialType;
    
    @NotBlank(message = "Color is required")
    private String color;
    
    @NotNull(message = "Required quantity is required")
    @Positive(message = "Required quantity must be positive")
    private Double requiredQuantityKg;
    
    @NotBlank(message = "Buyer address is required")
    private String buyerAddress;
    
    @NotNull(message = "Buyer latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double buyerLatitude;
    
    @NotNull(message = "Buyer longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double buyerLongitude;
}