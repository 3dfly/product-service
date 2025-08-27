package com.threedfly.shopify.dto;

import lombok.Data;

@Data
public class Metafield {
    private String namespace;
    private String key;
    private String type;   // e.g., "single_line_text_field", "number_integer", "list.single_line_text_field"
    private String value;  // stringified value per type
}
