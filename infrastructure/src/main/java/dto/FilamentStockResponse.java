package dto;

import entity.FilamentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilamentStockResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private FilamentType materialType;
    private String color;
    private Double quantityKg;
    private Double reservedKg;
    private boolean available;
    private Date lastRestocked;
    private Date expiryDate;
    private Double availableQuantityKg;
}