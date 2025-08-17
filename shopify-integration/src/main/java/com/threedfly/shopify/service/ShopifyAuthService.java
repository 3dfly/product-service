package com.threedfly.shopify.service;

import com.threedfly.shopify.config.ShopifyConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ShopifyAuthService {
    private final ShopifyConfig cfg;
    public ShopifyAuthService(ShopifyConfig cfg){ this.cfg = cfg; }

    public String newState(){ return UUID.randomUUID().toString(); }

    public String buildInstallUrl(String shopDomain, String state){
        return UriComponentsBuilder
                .fromHttpUrl("https://" + shopDomain + "/admin/oauth/authorize")
                .queryParam("client_id", cfg.apiKey)
                .queryParam("scope", cfg.scopes)
                .queryParam("redirect_uri", URLEncoder.encode(cfg.redirectUri, StandardCharsets.UTF_8))
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }
}