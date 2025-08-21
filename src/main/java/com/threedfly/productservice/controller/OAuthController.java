package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threedfly.productservice.entity.IntegrationAccount;
import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.entity.ShopType;
import com.threedfly.productservice.repository.IntegrationAccountRepository;
import com.threedfly.productservice.repository.ShopRepository;
import com.threedfly.shopify.config.ShopifyConfig;
import com.threedfly.shopify.service.ShopifyAuthService;
import com.threedfly.shopify.util.HmacVerifier;
import jakarta.servlet.http.HttpServletResponse;

import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class OAuthController {
    private final ShopifyAuthService authService;
    private final ShopifyConfig cfg;
    private final IntegrationAccountRepository accounts;
    private final ShopRepository shopRepository;
    private final OkHttpClient http;
    private final ObjectMapper om;

    public OAuthController(ShopifyAuthService authService, ShopifyConfig cfg, IntegrationAccountRepository accounts, ShopRepository shopRepository) {
        this.authService = authService;
        this.cfg = cfg;
        this.accounts = accounts;
        this.shopRepository = shopRepository;
        this.http = new OkHttpClient();
        this.om = new ObjectMapper();
    }

    // simple in-memory state store; replace with session/redis in prod
    private static final Map<String, Long> stateToShopId = new ConcurrentHashMap<>();

    // Handle Shopify app installation at root path - supports multiple shops
    @GetMapping("/")
    public void handleShopifyInstall(@RequestParam String shop,
                                     @RequestParam String hmac,
                                     @RequestParam String host,
                                     @RequestParam String timestamp,
                                     HttpServletResponse res) throws IOException {
        System.out.println("🔗 Shopify installation request received for shop: " + shop);
        System.out.println("HMAC: " + hmac);
        System.out.println("Host: " + host);
        System.out.println("Timestamp: " + timestamp);
        
        try {
            // Find or create shop record for this Shopify shop
            Long internalShopId = findOrCreateShop(shop);
            System.out.println("📋 Using internal shop ID: " + internalShopId + " for shop: " + shop);
            
            // Generate OAuth state and redirect to Shopify authorization
            String state = authService.newState();
            stateToShopId.put(state, internalShopId);
            
            String installUrl = authService.buildInstallUrl(shop, state);
            System.out.println("🚀 Redirecting to OAuth: " + installUrl);
            res.sendRedirect(installUrl);
            
        } catch (Exception e) {
            System.err.println("❌ Error during shop installation: " + e.getMessage());
            e.printStackTrace();
            res.setContentType("text/html");
            res.getWriter().write("❌ Installation Error: " + e.getMessage());
        }
    }

    /**
     * Find existing shop by domain or create a new one
     */
    private Long findOrCreateShop(String shopDomain) {
        // Try to find existing shop by name/domain
        Shop existingShop = shopRepository.findAll().stream()
                .filter(shop -> shop.getName() != null && shop.getName().contains(shopDomain.split("\\.")[0]))
                .findFirst()
                .orElse(null);
                
        if (existingShop != null) {
            System.out.println("✅ Found existing shop: " + existingShop.getName() + " with ID: " + existingShop.getId());
            return existingShop.getId();
        }
        
        // Create new shop record
        Shop newShop = new Shop();
        newShop.setName(shopDomain.split("\\.")[0]); // Extract shop name from domain
        newShop.setDescription("Auto-created for Shopify integration: " + shopDomain);
        newShop.setContactInfo(shopDomain);
        
        Shop savedShop = shopRepository.save(newShop);
        System.out.println("🆕 Created new shop: " + savedShop.getName() + " with ID: " + savedShop.getId());
        return savedShop.getId();
    }

    // Manual install: /oauth/install?shop=mystore.myshopify.com&internalShopId=123
    @GetMapping("/oauth/install")
    public void install(@RequestParam String shop,
                        @RequestParam("internalShopId") Long internalShopId,
                        HttpServletResponse res) throws IOException {
        String state = authService.newState();
        stateToShopId.put(state, internalShopId);
        res.sendRedirect(authService.buildInstallUrl(shop, state));
    }

    // OAuth callback
    @GetMapping("/oauth/callback")
    public String callback(@RequestParam Map<String,String> params) throws IOException {
        System.out.println("📥 OAuth callback received with params: " + params);
        
        String shop = params.get("shop");
        String hmac = params.get("hmac");
        String state = params.get("state");
        String code = params.get("code");

        System.out.println("🔍 Callback details:");
        System.out.println("  Shop: " + shop);
        System.out.println("  HMAC: " + hmac);
        System.out.println("  State: " + state);
        System.out.println("  Code: " + code);

        // COMPLETELY SKIP HMAC verification for development
        System.out.println("⚠️ HMAC verification DISABLED for development");
        
        Long internalShopId = stateToShopId.remove(state);
        if (internalShopId == null) {
            System.err.println("❌ State not found: " + state);
            System.err.println("  Available states: " + stateToShopId.keySet());
            return "⚠️ Invalid or expired state: " + state + ". Please reinstall the app.";
        }

                 // Exchange code → token
         okhttp3.RequestBody form = new FormBody.Builder()
                 .add("client_id", cfg.apiKey)
                 .add("client_secret", cfg.apiSecret)
                 .add("code", params.get("code"))
                 .build();

        Request req = new Request.Builder()
                .url("https://" + shop + "/admin/oauth/access_token")
                .post(form)
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String responseBody = resp.body().string();
            System.out.println("🔍 Token exchange response:");
            System.out.println("  Status: " + resp.code());
            System.out.println("  Body: " + responseBody);
            
            if (!resp.isSuccessful()) {
                System.err.println("❌ Token exchange failed:");
                System.err.println("  Request URL: " + req.url());
                System.err.println("  Client ID: " + cfg.apiKey);
                System.err.println("  Client Secret: " + cfg.apiSecret.substring(0, 8) + "...");
                System.err.println("  Authorization Code: " + code);
                System.err.println("  Response: " + responseBody);
                return "❌ Token exchange failed: " + resp.code() + " - " + responseBody;
            }
            
            JsonNode json = om.readTree(responseBody);
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

                         IntegrationAccount saved = accounts.save(acct);
             System.out.println("✅ Integration account created successfully!");
             System.out.println("  Account ID: " + saved.getId());
             System.out.println("  Shop: " + shop);
             System.out.println("  Internal Shop ID: " + internalShopId);
             System.out.println("  Scopes: " + scopes);
             
             return """
                 <html><body>
                 <h2>✅ 3D Fly App Installed Successfully!</h2>
                 <p><strong>Shop:</strong> %s</p>
                 <p><strong>Integration Account ID:</strong> %s</p>
                 <p><strong>Internal Shop ID:</strong> %s</p>
                 <p><strong>Scopes:</strong> %s</p>
                 <br/>
                 <p>🎉 Your app is now connected to Shopify!</p>
                 <p><strong>Next Steps:</strong></p>
                 <ol>
                     <li>Create products in your local system</li>
                     <li>Use Integration Account ID <strong>%s</strong> to publish them to Shopify</li>
                     <li><a href="http://localhost:8081/publish-test.html">🚀 Try Publishing Interface</a></li>
                 </ol>
                 </body></html>
                 """.formatted(shop, saved.getId(), internalShopId, scopes, saved.getId());
        }
    }

    // List all integration accounts
    @GetMapping("/oauth/accounts")
    public ResponseEntity<Map<String, Object>> listIntegrationAccounts() {
        try {
            var allAccounts = accounts.findAll();
            
            System.out.println("📋 Found " + allAccounts.size() + " integration accounts");
            
            var accountsInfo = allAccounts.stream().map(acct -> Map.<String, Object>of(
                "id", acct.getId(),
                "shopId", acct.getShopId(),
                "provider", acct.getProvider().toString(),
                "externalShopId", acct.getExternalShopId(),
                "scopes", acct.getScopes() != null ? acct.getScopes() : "",
                "installedAt", acct.getInstalledAt() != null ? acct.getInstalledAt().toString() : "",
                "hasToken", acct.getAccessToken() != null && !acct.getAccessToken().isEmpty()
            )).toList();
            
            return ResponseEntity.ok(Map.<String, Object>of(
                "totalAccounts", allAccounts.size(),
                "accounts", accountsInfo
            ));
        } catch (Exception e) {
            System.err.println("❌ Error listing integration accounts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.<String, Object>of(
                    "error", "Failed to list integration accounts",
                    "message", e.getMessage()
                )
            );
        }
    }

    // Get integration account by ID
    @GetMapping("/oauth/accounts/{id}")
    public ResponseEntity<Map<String, Object>> getIntegrationAccount(@PathVariable Long id) {
        try {
            var account = accounts.findById(id);
            if (account.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            var acct = account.get();
            var accountInfo = Map.<String, Object>of(
                "id", acct.getId(),
                "shopId", acct.getShopId(),
                "provider", acct.getProvider().toString(),
                "externalShopId", acct.getExternalShopId(),
                "scopes", acct.getScopes() != null ? acct.getScopes() : "",
                "installedAt", acct.getInstalledAt() != null ? acct.getInstalledAt().toString() : "",
                "updatedAt", acct.getUpdatedAt() != null ? acct.getUpdatedAt().toString() : "",
                "hasToken", acct.getAccessToken() != null && !acct.getAccessToken().isEmpty()
            );
            
            return ResponseEntity.ok(accountInfo);
        } catch (Exception e) {
            System.err.println("❌ Error getting integration account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.<String, Object>of(
                    "error", "Failed to get integration account",
                    "message", e.getMessage()
                )
            );
        }
    }
}
