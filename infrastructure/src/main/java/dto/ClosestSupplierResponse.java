package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosestSupplierResponse {
    
    private SupplierResponse supplier;
    private FilamentStockResponse availableStock;
    private Double distanceKm;
    private String message;
    
    public static ClosestSupplierResponse success(SupplierResponse supplier, FilamentStockResponse stock, Double distance) {
        return ClosestSupplierResponse.builder()
                .supplier(supplier)
                .availableStock(stock)
                .distanceKm(distance)
                .message("Closest supplier found successfully")
                .build();
    }
    
    public static ClosestSupplierResponse noSupplierFound(String reason) {
        return ClosestSupplierResponse.builder()
                .message("No suitable supplier found: " + reason)
                .build();
    }
}