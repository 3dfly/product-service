package com.threedfly.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopRequest {
    @NotNull(message = "Seller ID is required")
    private Long sellerId;
    
    @NotBlank(message = "Shop name is required")
    private String name;
    
    private String description;
    private String address;
    private String contactInfo;
}