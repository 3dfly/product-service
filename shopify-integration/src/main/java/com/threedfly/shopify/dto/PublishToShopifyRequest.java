package com.threedfly.shopify.dto;

import dto.PublishToStoreRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublishToShopifyRequest extends PublishToStoreRequest {

    // Shopify-specific fields (basics are inherited from PublishToStoreRequest)
    private String descriptionHtml;

    // Product state & organization
    private String vendor;            // e.g., "My Store"
    private String productType;       // free text
    private List<String> tags;        // ["pokemon","3d-print"]
    private String templateSuffix;    // e.g., "default"

    // Category (Shopify taxonomy)
    private String productCategoryId; // GID of ProductCategory (optional)

    // Media by public URL (inherited from parent: imageUrls)

    // Variants & options
    private List<String> options;     // e.g., ["Color","Size"] (max 3)
    private List<Variant> variants;   // must align optionValues with options[] order

    // Inventory (single location; if provided, we set quantities)
    private Inventory inventory;

    // Collections (manual only)
    private Collections collections;

    // SEO
    private Seo seo;

    // Metafields
    private List<Metafield> metafields;

    // --- Optional simple fallbacks (if you don't pass variants) ---
    // These are used only when variants == null or empty:
    // price is inherited as BigDecimal from parent
    private String sku;               // "TEE-001"
    private String barcode;           // optional
    private String compareAtPrice;    // optional

}

