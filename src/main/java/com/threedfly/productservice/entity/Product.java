package com.threedfly.productservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    private String description;

    private double price;

    private String imageUrl;

    private String stlFileUrl; // For 3D printing products

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long sellerId; // Reference to Seller in order-service
    private Long shopId;   // Reference to Shop in this service

    @ManyToOne
    @JoinColumn(name = "shopId", insertable = false, updatable = false)
    private Shop shop;
} 