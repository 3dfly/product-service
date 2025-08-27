package com.threedfly.shopify.dto;

import lombok.Data;

@Data
public class Variant {
    private java.util.List<String> optionValues; // aligns with options[]
    private String sku;
    private String barcode;
    private String price;
    private String compareAtPrice;
    private Integer quantity; // optional; used if inventory.locationId present
}

