package com.threedfly.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dto.PublishToStoreRequest;
import entity.IntegrationAccount;
import entity.Product;
import entity.ProductSync;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import provider.StoreProvider;
import service.StoreUtils;

import java.util.Map;

@Service("shopify")
@AllArgsConstructor
public class ShopifyProvider implements StoreProvider {

    private final ObjectMapper om;
    private final ShopifyGraphQLService shopifyGraphQLService;

    @Override
    public ResponseEntity<?> publishProduct(PublishToStoreRequest req, Product product, IntegrationAccount integrationAccount) throws Exception {
        System.out.println("🔍 Publishing product: ID=" + product.getId() + ", Name=" + product.getName() + ", Price=" + product.getPrice());
        
        // Create product in Shopify
        String createRaw = createProduct(req, integrationAccount, product);

        JsonNode createJson = om.readTree(createRaw);
        String productGid = createJson.at("/data/productCreate/product/id").asText();
        String defaultVariantId = createJson.at("/data/productCreate/product/variants/nodes/0/id").asText();

        // Set product price
        setVariantPrice(defaultVariantId, product, productGid, integrationAccount);

        // Set media for product using stored image data
        setProductMedia(product, productGid, integrationAccount);

        // Publish to online store
        var isPublished = publishToOnlineStore(integrationAccount.getExternalShopId(), integrationAccount.getAccessToken(), productGid);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "published", isPublished,
            "shopifyProductId", productGid,
            "message", "Product published successfully to Shopify"
        ));
    }

    @Override
    public ResponseEntity<?> deleteProduct(Product product, ProductSync syncRecord, IntegrationAccount account) {
        String shopifyProductId = syncRecord.getExternalProductId();

        // 4) Delete from Shopify using GraphQL
        String deleteMutation = """
                mutation productDelete($input: ProductDeleteInput!) {
                  productDelete(input: $input) {
                    deletedProductId
                    userErrors { field message }
                  }
                }
            """;

        ObjectMapper om = new ObjectMapper();
        ObjectNode deleteInput = om.createObjectNode();
        deleteInput.put("id", shopifyProductId);

        ObjectNode variables = om.createObjectNode();
        variables.set("input", deleteInput);

        try {
            String deleteResponse = shopifyGraphQLService.postGraphQL(
                    account.getExternalShopId(),
                    account.getAccessToken(),
                    deleteMutation,
                    om.writeValueAsString(variables)
            );

            System.out.println("🗑️ Shopify deletion response: " + deleteResponse);

            // Check for errors
            JsonNode deleteJson = om.readTree(deleteResponse);
            JsonNode errors = deleteJson.path("data").path("productDelete").path("userErrors");
            if (errors.isArray() && errors.size() > 0) {
                System.err.println("❌ Shopify deletion errors: " + errors.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Failed to delete from Shopify",
                        "details", errors.toString()
                ));
            }

            String deletedId = deleteJson.path("data").path("productDelete").path("deletedProductId").asText();
            System.out.println("✅ Successfully deleted from Shopify: " + deletedId);

        } catch (Exception e) {
            System.err.println("❌ Error deleting from Shopify: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to delete from Shopify",
                    "message", e.getMessage()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Product deleted successfully from both Shopify and local database",
                "productId", product.getId(),
                "shopifyDeleted", syncRecord
        ));
    }

    private String createProduct(PublishToStoreRequest request, IntegrationAccount acct, Product product) throws Exception {
        String createMutation = """
                mutation CreateProduct($product: ProductCreateInput!, $media: [CreateMediaInput!]) {
                  productCreate(product: $product, media: $media) {
                    product {
                      id
                      variants(first: 1) { nodes { id inventoryItem { id } } }
                    }
                    userErrors { field message }
                  }
                }
                """;

        // Use Product entity data directly (title, description, price come from stored product)
        String title = product.getName();
        String descriptionHtml = product.getDescription();
        
        // Shopify-specific fields from request
        String status = StoreUtils.coalesce(request.getStatus(), "ACTIVE"); // ACTIVE or DRAFT

        ObjectNode productNode = om.createObjectNode()
                .put("title", title)                       // From Product entity
                .put("status", status)                     // From request (Shopify-specific)
                .put("descriptionHtml", descriptionHtml == null ? "" : descriptionHtml); // From Product entity
                
//        // Add vendor if provided in request (Shopify-specific field)
//        if (request instanceof com.threedfly.shopify.dto.PublishToShopifyRequest) {
//            com.threedfly.shopify.dto.PublishToShopifyRequest shopifyRequest = (com.threedfly.shopify.dto.PublishToShopifyRequest) request;
//            if (shopifyRequest.getVendor() != null && !shopifyRequest.getVendor().trim().isEmpty()) {
//                productNode.put("vendor", shopifyRequest.getVendor().trim());
//            }
//        }

//        if (productType != null) product.put("productType", productType);
//        if (templateSuffix != null) product.put("templateSuffix", templateSuffix);
//
//// optional: tags
//        if (tags != null && !tags.isEmpty()) {
//            ArrayNode t = om.createArrayNode();
//            tags.forEach(t::add);
//            product.set("tags", t);
//        }
//
//// optional: join collections on create
//        if (request.getCollections() != null && request.getCollections().getCollectionIds() != null
//                && !request.getCollections().getCollectionIds().isEmpty()) {
//            ArrayNode ids = om.createArrayNode();
//            request.getCollections().getCollectionIds().forEach(ids::add);
//            product.set("collectionsToJoin", ids);
//        }
//
//// optional: define options up-front (instead of a later productOptionsCreate)
//        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
//            ArrayNode productOptions = om.createArrayNode();
//            for (String name : request.getOptions()) {
//                ObjectNode option = om.createObjectNode().put("name", name);
//                productOptions.add(option);
//            }
//            product.set("productOptions", productOptions);
//        }

        ObjectNode createVars = om.createObjectNode();
        createVars.set("product", productNode);

        String createRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), createMutation, om.writeValueAsString(createVars)
        );
        return createRaw;
    }

    private void setVariantPrice(String defaultVariantId, Product product, String productGid, IntegrationAccount acct) throws Exception {
        String bulkUpdate = """
mutation SetVariantPrice($productId: ID!, $variants: [ProductVariantsBulkInput!]!) {
  productVariantsBulkUpdate(productId: $productId, variants: $variants) {
    product { id variants(first: 10) { nodes { id price inventoryItem { id } } } }
    userErrors { field message }
  }
}
""";

        ArrayNode variantsArr = om.createArrayNode();
        ObjectNode v = om.createObjectNode();
        v.put("id", defaultVariantId);
        v.put("price", String.format("%.2f", product.getPrice())); // Use Product entity price
        variantsArr.add(v);

        ObjectNode bulkVars = om.createObjectNode();
        bulkVars.put("productId", productGid);
        bulkVars.set("variants", variantsArr);

        String bulkRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), bulkUpdate, om.writeValueAsString(bulkVars)
        );
    }

    private void setProductMedia(Product product, String productGid, IntegrationAccount acct) throws Exception {
        // Check if product has image data
        if (product.getImageData() == null || product.getImageData().length == 0) {
            System.out.println("⚠️ No image data found for product: " + product.getName());
            return;
        }

        // Generate public URL for the stored image
        String baseUrl = getNgrokUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8081"; // fallback
        }
        String imageUrl = baseUrl + "/products/" + product.getId() + "/image";
        
        System.out.println("📸 Using stored product image: " + imageUrl);

        String updateMedia = """
mutation UpdateProductWithNewMedia($product: ProductUpdateInput!, $media: [CreateMediaInput!]) {
  productUpdate(product: $product, media: $media) {
    product { id media(first: 10) { nodes { alt mediaContentType preview { status } } } }
    userErrors { field message }
  }
}
""";

        ObjectNode productInput = om.createObjectNode().put("id", productGid);
        ArrayNode mediaToAdd = om.createArrayNode();
        
        // Add the image to Shopify
        ObjectNode m = om.createObjectNode();
        m.put("originalSource", imageUrl);
        m.put("mediaContentType", "IMAGE");
        if (product.getImageFilename() != null) {
            m.put("alt", product.getImageFilename());
        }
        mediaToAdd.add(m);
        System.out.println("📸 Adding image to Shopify: " + imageUrl);

        ObjectNode vars = om.createObjectNode();
        vars.set("product", productInput);
        vars.set("media", mediaToAdd);

        String mediaRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), updateMedia, om.writeValueAsString(vars)
        );
        
        System.out.println("✅ Product media updated in Shopify");
    }
    
    private String getNgrokUrl() {
        // First try system property (set by NgrokAutoStartService)
        String ngrokUrl = System.getProperty("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }
        
        // Fallback to environment variable
        return System.getenv("NGROK_URL");
    }

    private boolean publishToOnlineStore(String shopDomain, String accessToken, String productGid) throws Exception {
        String onlineStorePublicationId = fetchOnlineStorePublicationId(shopDomain, accessToken);
        if (onlineStorePublicationId == null) {
            throw new IllegalStateException("Could not find 'Online Store' publication on this shop");
        }
        return publishProduct(shopDomain, accessToken, productGid, onlineStorePublicationId);
    }

    private String fetchOnlineStorePublicationId(String shopDomain, String accessToken) throws Exception {
        String publicationsQuery = """
    query {
      publications(first: 25) {
        edges { node { id name } }
      }
    }
    """;

        // No variables for this query → pass null
        String publicationsRaw = shopifyGraphQLService.postGraphQL(shopDomain, accessToken, publicationsQuery, null);

        JsonNode root = om.readTree(publicationsRaw);

        // GraphQL-level errors?
        JsonNode gqlErrors = root.path("errors");
        if (gqlErrors.isArray() && gqlErrors.size() > 0) {
            throw new RuntimeException("GraphQL errors: " + gqlErrors.toString());
        }

        JsonNode edges = root.path("data").path("publications").path("edges");
        if (!edges.isArray()) return null;

        for (JsonNode edge : edges) {
            String name = edge.path("node").path("name").asText();
            if ("Online Store".equals(name)) {
                return edge.path("node").path("id").asText();
            }
        }
        return null; // not found
    }

    private boolean publishProduct(String shopDomain, String accessToken, String productGid, String publicationId) throws Exception {
        if (publicationId == null || publicationId.isBlank()) {
            throw new IllegalArgumentException("publicationId is null/blank");
        }

        String publishMutation = """
    mutation Publish($id: ID!, $input: [PublicationInput!]!) {
      publishablePublish(id: $id, input: $input) {
        publishable { publicationCount }
        userErrors { field message }
      }
    }
    """;

        // Build variables JSON with Jackson
        ObjectNode vars = om.createObjectNode();
        vars.put("id", productGid);

        ArrayNode inputArr = om.createArrayNode();
        ObjectNode pub = om.createObjectNode();
        pub.put("publicationId", publicationId);
        inputArr.add(pub);
        vars.set("input", inputArr);

        String variablesJson = om.writeValueAsString(vars);

        String publishRaw = shopifyGraphQLService.postGraphQL(shopDomain, accessToken, publishMutation, variablesJson);

        JsonNode root = om.readTree(publishRaw);

        // GraphQL-level errors?
        JsonNode gqlErrors = root.path("errors");
        if (gqlErrors.isArray() && gqlErrors.size() > 0) {
            throw new RuntimeException("GraphQL errors: " + gqlErrors.toString());
        }

        JsonNode result = root.path("data").path("publishablePublish");
        JsonNode userErrors = result.path("userErrors");
        if (userErrors.isArray() && userErrors.size() > 0) {
            throw new RuntimeException("Publish userErrors: " + userErrors.toString());
        }

        int count = result.path("publishable").path("publicationCount").asInt();
        return count > 0;
    }
}
