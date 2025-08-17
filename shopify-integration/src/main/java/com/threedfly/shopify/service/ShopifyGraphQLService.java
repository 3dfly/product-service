// src/main/java/com/threedfly/shopify/service/ShopifyGraphQLService.java
package com.threedfly.shopify.service;

import com.threedfly.shopify.config.ShopifyConfig;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
public class ShopifyGraphQLService {
    private final OkHttpClient http = new OkHttpClient();
    private final ShopifyConfig cfg;
    public ShopifyGraphQLService(ShopifyConfig cfg){ this.cfg = cfg; }

    public String postGraphQL(String shopDomain, String accessToken, String graphql, String variablesJson) throws Exception {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String body = "{\"query\":" + quote(graphql) + (variablesJson!=null? ",\"variables\":" + variablesJson : "") + "}";
        Request req = new Request.Builder()
                .url("https://" + shopDomain + "/admin/api/" + cfg.apiVersion + "/graphql.json")
                .addHeader("X-Shopify-Access-Token", accessToken)
                .post(RequestBody.create(body, JSON))
                .build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new RuntimeException("GraphQL error: " + resp.code() + " " + resp.message());
            return resp.body().string();
        }
    }

    private static String quote(String s){
        return "\"" + s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n") + "\"";
    }
}
