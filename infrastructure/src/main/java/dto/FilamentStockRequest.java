package dto;

import entity.FilamentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilamentStockRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    
    @NotNull(message = "Material type is required")
    private FilamentType materialType;
    
    @NotBlank(message = "Color is required")
    private String color;
    
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Double quantityKg;
    
    @PositiveOrZero(message = "Reserved quantity must be zero or positive")
    private Double reservedKg;
    
    @Builder.Default
    private boolean available = true;
    
    private Date lastRestocked;
    private Date expiryDate;
}