package com.threedfly.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "product_sync",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_account",
                columnNames = {"product_id","integration_account_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSync {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId; // your internal Product.id

    @Column(name = "integration_account_id", nullable = false)
    private Long integrationAccountId; // FK to IntegrationAccount.id

    @Column(length = 255)
    private String externalProductId;       // Shopify product GID
    @Column(length = 255)
    private String externalVariantId;       // first/default variant GID
    @Column(length = 255)
    private String externalInventoryItemId; // inventory item GID

    @Column(length = 32)
    private String syncState;               // CREATED/UPDATED/FAILED
    private Instant lastSyncedAt;
    @Column(length = 2000)
    private String lastError;
}

