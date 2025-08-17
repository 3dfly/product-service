package com.threedfly.shopify.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PublishToShopifyRequest {
    @NotNull
    private Long integrationAccountId;

    // Basics (fallbacks to your local product if null)
    private String title;
    private String descriptionHtml;

    // Product state & organization
    private String status;            // "ACTIVE" | "DRAFT" (default ACTIVE if null)
    private String vendor;            // e.g., "My Store"
    private String productType;       // free text
    private List<String> tags;        // ["pokemon","3d-print"]
    private String templateSuffix;    // e.g., "default"

    // Category (Shopify taxonomy)
    private String productCategoryId; // GID of ProductCategory (optional)

    // Media by public URL (Shopify fetches them)
    private List<String> imageUrls;

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
    private String price;             // "29.99"
    private String sku;               // "TEE-001"
    private String barcode;           // optional
    private String compareAtPrice;    // optional

}

