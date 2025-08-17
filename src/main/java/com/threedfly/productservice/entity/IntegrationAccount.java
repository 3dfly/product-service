package com.threedfly.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "integration_account",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_integration_shop_provider_ext",
                columnNames = {"shop_id", "provider", "external_shop_id"}
        )
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IntegrationAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to your clean Shop
    @Column(name = "shop_id", nullable = false)
    private Long shopId; // FK to your core Shop.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShopType provider;

    // e.g. "mystore.myshopify.com" or marketplace merchant ID
    @Column(name = "external_shop_id", nullable = false, length = 255)
    private String externalShopId;

    // Admin/API access token or credential material (ENCRYPT THIS COLUMN)
    @Lob
    @Column(name = "access_token", nullable = false)
    private String accessToken;

    // optional: comma-separated scopes or JSON
    @Column(length = 1000)
    private String scopes;

    private Instant installedAt;
    private Instant updatedAt;

    // optional: per-provider metadata JSON (store domain, api version, etc.)
    @Lob
    private String metadataJson;
}
