package com.threedfly.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilamentStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Detailed material specifications
    @Enumerated(EnumType.STRING)
    private FilamentType materialType; // PLA, ABS, PETG, TPU, etc.

    private String color;

    // Quantity and availability
    private Double quantityKg; // Available quantity in kilograms
    @Builder.Default
    private Double reservedKg = 0.0; // Currently reserved/allocated quantity

    @Builder.Default
    private boolean available = true;

    // Inventory tracking
    private Date lastRestocked;
    private Date expiryDate;

    public Double getAvailableQuantityKg() {
        return quantityKg - (reservedKg != null ? reservedKg : 0.0);
    }

    public boolean hasEnoughStock(Double requiredKg) {
        return getAvailableQuantityKg() >= requiredKg;
    }
}