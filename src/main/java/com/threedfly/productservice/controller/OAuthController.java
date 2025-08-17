package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threedfly.productservice.entity.IntegrationAccount;
import com.threedfly.productservice.entity.ShopType;
import com.threedfly.productservice.repository.IntegrationAccountRepository;
import com.threedfly.shopify.config.ShopifyConfig;
import com.threedfly.shopify.service.ShopifyAuthService;
import com.threedfly.shopify.util.HmacVerifier;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import okhttp3.*;
import okhttp3.RequestBody;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/oauth")
@AllArgsConstructor
public class OAuthController {
    private final ShopifyAuthService authService;
    private final ShopifyConfig cfg;
    private final IntegrationAccountRepository accounts;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    // simple in-memory state store; replace with session/redis in prod
    private static final Map<String, Long> stateToShopId = new ConcurrentHashMap<>();

    // Start install: /oauth/install?shop=mystore.myshopify.com&internalShopId=123
    @GetMapping("/install")
    public void install(@RequestParam String shop,
                        @RequestParam("internalShopId") Long internalShopId,
                        HttpServletResponse res) throws IOException {
        String state = authService.newState();
        stateToShopId.put(state, internalShopId);
        res.sendRedirect(authService.buildInstallUrl(shop, state));
    }

    // OAuth callback
    @GetMapping("/callback")
    public String callback(@RequestParam Map<String,String> params) throws IOException {
        String shop = params.get("shop");
        String hmac = params.get("hmac");
        String state = params.get("state");

        if (!HmacVerifier.verifyQueryString(params, cfg.apiSecret, hmac)) {
            return "Invalid HMAC";
        }
        Long internalShopId = stateToShopId.remove(state);
        if (internalShopId == null) {
            return "Invalid or expired state";
        }

        // Exchange code → token
        RequestBody form = new FormBody.Builder()
                .add("client_id", cfg.apiKey)
                .add("client_secret", cfg.apiSecret)
                .add("code", params.get("code"))
                .build();

        Request req = new Request.Builder()
                .url("https://" + shop + "/admin/oauth/access_token")
                .post(form)
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) return "Token exchange failed: " + resp.code();
            JsonNode json = om.readTree(resp.body().string());
            String token = json.get("access_token").asText();
            String scopes = json.path("scope").asText("");

            IntegrationAccount acct = accounts
                    .findByShopIdAndProviderAndExternalShopId(internalShopId, ShopType.SHOPIFY, shop)
                    .orElseGet(IntegrationAccount::new);

            acct.setShopId(internalShopId);
            acct.setProvider(ShopType.SHOPIFY);
            acct.setExternalShopId(shop);
            acct.setAccessToken(token);
            acct.setScopes(scopes);
            acct.setInstalledAt(Instant.now());
            acct.setUpdatedAt(Instant.now());

            accounts.save(acct);
            return "Installed for " + shop + " (internal shop " + internalShopId + ") with scopes: " + scopes;
        }
    }
}
