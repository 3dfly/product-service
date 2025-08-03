package com.threedfly.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Reference to User in auth-service
    private String name;
    private String email;
    private String phone;
    private String address;

    // Enhanced location information for distance calculations
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // Geographic coordinates for precise distance calculations
    private Double latitude;
    private Double longitude;

    // Business information
    private String businessLicense;
    private String description;
    private boolean verified;
    private boolean active;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FilamentStock> stock = new java.util.ArrayList<>();
}
