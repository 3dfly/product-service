package com.threedfly.shopify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyConfig {
    @Value("${shopify.apiKey}")
    public String apiKey;
    @Value("${shopify.apiSecret}")
    public String apiSecret;
    @Value("${shopify.redirectUri}")
    public String redirectUri; // e.g., http://localhost:8080/oauth/callback
    @Value("${shopify.apiVersion:2025-07}")
    public String apiVersion;  // keep current per Shopify versioning
    @Value("${shopify.scopes}")
    public String scopes;      // e.g., write_products,read_products,write_inventory
}
