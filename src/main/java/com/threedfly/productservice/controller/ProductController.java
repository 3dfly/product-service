package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.threedfly.productservice.dto.ProductRequest;
import com.threedfly.productservice.dto.ProductResponse;

import com.threedfly.productservice.entity.IntegrationAccount;
import com.threedfly.productservice.entity.Product;
import com.threedfly.productservice.entity.ProductSync;
import com.threedfly.productservice.entity.ShopType;
import com.threedfly.productservice.repository.IntegrationAccountRepository;
import com.threedfly.productservice.repository.ProductRepository;
import com.threedfly.productservice.repository.ProductSyncRepository;

import com.threedfly.productservice.service.ProductService;
import com.threedfly.shopify.dto.PublishToShopifyRequest;
import com.threedfly.shopify.service.ShopifyGraphQLService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Validated
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final IntegrationAccountRepository integrationAccountRepository;
    private final ShopifyGraphQLService shopifyGraphQLService;
    private final ProductSyncRepository productSyncRepository;
    private final ObjectMapper om;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8081}")
    private String serverPort;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable @NotNull Long id) {
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    // Simple manual setup endpoint for Shopify integration
    @PostMapping("/setup-shopify")
    public ResponseEntity<Map<String, Object>> setupShopify(
            @RequestParam String shopDomain,
            @RequestParam String accessToken) {
        try {
            IntegrationAccount acct = new IntegrationAccount();
            acct.setShopId(1L); // Use shop ID 1 (just created)
            acct.setProvider(ShopType.SHOPIFY);
            acct.setExternalShopId(shopDomain);
            acct.setAccessToken(accessToken);
            acct.setScopes("read_products,write_products,read_inventory,write_inventory,read_publications,write_publications,write_files");
            acct.setInstalledAt(java.time.Instant.now());
            acct.setUpdatedAt(java.time.Instant.now());

            IntegrationAccount saved = integrationAccountRepository.save(acct);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Integration account created successfully!",
                    "shopDomain", shopDomain,
                    "accountId", saved.getId(),
                    "scopes", saved.getScopes()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Log the full error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to create integration account",
                    "message", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    // Test Shopify connection
    @PostMapping("/test-shopify-connection")
    public ResponseEntity<Map<String, Object>> testShopifyConnection(@RequestParam Long integrationAccountId) {
        try {
            IntegrationAccount acct = integrationAccountRepository.findById(integrationAccountId)
                    .orElseThrow(() -> new RuntimeException("Integration account not found: " + integrationAccountId));

            // Simple GraphQL query to test connection
            String testQuery = """
                    query {
                      shop {
                        name
                        url
                        plan {
                          displayName
                        }
                      }
                    }
                    """;

            String response = shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(),
                    acct.getAccessToken(),
                    testQuery,
                    "{}"
            );

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Shopify connection successful!",
                    "shopDomain", acct.getExternalShopId(),
                    "response", response
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to connect to Shopify",
                    "message", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProductWithFiles(
            @RequestPart("product") @Valid ProductRequest request,
            @RequestParam(value = "stlFile", required = false) MultipartFile stlFile,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        ProductResponse product = productService.save(request);

        // Process STL file if provided
        if (stlFile != null && !stlFile.isEmpty()) {
            String stlUrl = processUploadedStlFile(product.getId(), stlFile);

            // Update the product with STL URL
            Product productEntity = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            productEntity.setStlFileUrl(stlUrl);
            productRepository.save(productEntity);

            // Return updated product
            product = productService.findById(product.getId());
        }

        // Process image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            List<String> imageUrls = processUploadedImages(List.of(imageFile));
            if (!imageUrls.isEmpty()) {
                Product productEntity = productRepository.findById(product.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                productEntity.setImageUrl(imageUrls.get(0));
                productRepository.save(productEntity);

                // Return updated product
                product = productService.findById(product.getId());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable @NotNull Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(product);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProductWithFiles(
            @PathVariable @NotNull Long id,
            @RequestPart("product") @Valid ProductRequest request,
            @RequestParam(value = "stlFile", required = false) MultipartFile stlFile,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        ProductResponse product = productService.update(id, request);

        // Process STL file if provided
        if (stlFile != null && !stlFile.isEmpty()) {
            String stlUrl = processUploadedStlFile(id, stlFile);

            // Update the product with STL URL
            Product productEntity = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            productEntity.setStlFileUrl(stlUrl);
            productRepository.save(productEntity);

            // Return updated product
            product = productService.findById(id);
        }

        // Process image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            List<String> imageUrls = processUploadedImages(List.of(imageFile));
            if (!imageUrls.isEmpty()) {
                Product productEntity = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                productEntity.setImageUrl(imageUrls.get(0));
                productRepository.save(productEntity);

                // Return updated product
                product = productService.findById(id);
            }
        }

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NotNull Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/shopify")
    public ResponseEntity<?> deleteProductFromShopify(@PathVariable @NotNull Long id) throws Exception {
        System.out.println("🗑️ Starting deletion of product " + id + " from Shopify and local database");

        // 1) Get the product to find its Shopify ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // 2) Find the ProductSync record to get the Shopify product ID
        Optional<ProductSync> syncRecord = productSyncRepository.findByProductId(id);

        if (syncRecord.isPresent() && syncRecord.get().getExternalProductId() != null) {
            String shopifyProductId = syncRecord.get().getExternalProductId();
            Long integrationAccountId = syncRecord.get().getIntegrationAccountId();

            // 3) Get integration account details
            IntegrationAccount account = integrationAccountRepository.findById(integrationAccountId)
                    .orElseThrow(() -> new RuntimeException("Integration account not found: " + integrationAccountId));

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

            // 5) Delete the sync record
            productSyncRepository.delete(syncRecord.get());
            System.out.println("🗑️ Deleted ProductSync record");
        } else {
            System.out.println("⚠️ No Shopify sync record found - product may not be published to Shopify");
        }

        // 6) Delete from local database
        productService.delete(id);
        System.out.println("🗑️ Deleted from local database");

        return ResponseEntity.ok(Map.of(
                "message", "Product deleted successfully from both Shopify and local database",
                "productId", id,
                "shopifyDeleted", syncRecord.isPresent()
        ));
    }

    @PostMapping(value = "/{id}/publish/shopify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publishToShopifyJson(
            @PathVariable Long id,
            @RequestBody PublishToShopifyRequest req) throws Exception {
        return uploadProductToStore(id, req, null, null);
    }

    @PostMapping(value = "/{id}/publish/shopify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> publishToShopifyMultipart(
            @PathVariable Long id,
            @RequestPart("request") PublishToShopifyRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "stlFiles", required = false) List<MultipartFile> stlFiles) throws Exception {

        return uploadProductToStore(id, request, images, stlFiles);
    }

    private ResponseEntity<?> uploadProductToStore(Long id, PublishToShopifyRequest req, List<MultipartFile> images, List<MultipartFile> stlFiles) throws Exception {
        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            uploadedImageUrls = processUploadedImages(images);
        }

        // Process uploaded STL files if any
        if (stlFiles != null && !stlFiles.isEmpty() && stlFiles.get(0) != null && !stlFiles.get(0).isEmpty()) {
            String stlUrl = processUploadedStlFiles(id, stlFiles);
            // STL URL is stored in the product, not sent to Shopify
            System.out.println("STL file uploaded and saved to: " + stlUrl);
        } else {
            System.out.println("No STL files provided - skipping STL upload");
        }

        // Merge uploaded images with any provided image URLs
        List<String> allImageUrls = new ArrayList<>();
        if (req.getImageUrls() != null) {
            allImageUrls.addAll(req.getImageUrls());
        }
        allImageUrls.addAll(uploadedImageUrls);
        req.setImageUrls(allImageUrls);

        final PublishToShopifyRequest finalReq = req;

        // 1) Resolve Shopify account
        var acct = integrationAccountRepository.findById(finalReq.getIntegrationAccountId())
                .orElseThrow(() -> new IllegalArgumentException("integrationAccountId not found: " + finalReq.getIntegrationAccountId()));
        if (acct.getProvider() != ShopType.SHOPIFY) {
            throw new IllegalArgumentException("integrationAccountId is not SHOPIFY");
        }

        // 2) Pull your local product for defaults (adapt getters to your DTO)
        var p = productService.findById(id);
        System.out.println("🔍 Local product details: ID=" + p.getId() + ", Name=" + p.getName() + ", Price=" + p.getPrice());
        String createRaw = createProduct(finalReq, acct, p);

        JsonNode createJson = om.readTree(createRaw);
        String productGid = createJson.at("/data/productCreate/product/id").asText();
        String defaultVariantId = createJson.at("/data/productCreate/product/variants/nodes/0/id").asText();

        //Set product price
        setVariantPrice(defaultVariantId, p, productGid, acct);

        //Set media for product such images
        setProductMedia(req, images, p, productGid, acct);

        //publish into store
        var isPublished = publishToOnlineStore(acct.getExternalShopId(),acct.getAccessToken(), productGid);

        return null;
    }

    private String createProduct(PublishToShopifyRequest request, IntegrationAccount acct, ProductResponse p) throws Exception {
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

        String title = coalesce(request.getTitle(), p.getName());
        String descriptionHtml = coalesce(request.getDescriptionHtml(), p.getDescription());
        String vendor = coalesce(request.getVendor(), "YourPlatform");
        String productType = request.getProductType();
        var tags = request.getTags();
        String templateSuffix = request.getTemplateSuffix();
        String status = coalesce(request.getStatus(), "ACTIVE"); // ACTIVE or DRAFT

        ObjectNode product = om.createObjectNode()
                .put("title", title)                       // e.g. "Cool socks"
                .put("vendor", vendor)                     // e.g. "YourPlatform"
                .put("status", status)                     // ACTIVE or DRAFT
                .put("descriptionHtml", descriptionHtml == null ? "" : descriptionHtml);

        if (productType != null) product.put("productType", productType);
        if (templateSuffix != null) product.put("templateSuffix", templateSuffix);

// optional: tags
        if (tags != null && !tags.isEmpty()) {
            ArrayNode t = om.createArrayNode();
            tags.forEach(t::add);
            product.set("tags", t);
        }

// optional: join collections on create
        if (request.getCollections() != null && request.getCollections().getCollectionIds() != null
                && !request.getCollections().getCollectionIds().isEmpty()) {
            ArrayNode ids = om.createArrayNode();
            request.getCollections().getCollectionIds().forEach(ids::add);
            product.set("collectionsToJoin", ids);
        }

// optional: define options up-front (instead of a later productOptionsCreate)
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            ArrayNode productOptions = om.createArrayNode();
            for (String name : request.getOptions()) {
                ObjectNode option = om.createObjectNode().put("name", name);
                productOptions.add(option);
            }
            product.set("productOptions", productOptions);
        }


        ObjectNode createVars = om.createObjectNode();
        createVars.set("product", product);

        String createRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), createMutation, om.writeValueAsString(createVars)
        );
        return createRaw;
    }

    private void setVariantPrice(String defaultVariantId, ProductResponse p, String productGid, IntegrationAccount acct) throws Exception {
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
        v.put("price", String.format("%.2f", p.getPrice())); // or req price, string okay
        variantsArr.add(v);

        ObjectNode bulkVars = om.createObjectNode();
        bulkVars.put("productId", productGid);
        bulkVars.set("variants", variantsArr);

        String bulkRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), bulkUpdate, om.writeValueAsString(bulkVars)
        );
    }

    private void setProductMedia(PublishToShopifyRequest req, List<MultipartFile> images, ProductResponse p, String productGid, IntegrationAccount acct) throws Exception {
        String updateMedia = """
mutation UpdateProductWithNewMedia($product: ProductUpdateInput!, $media: [CreateMediaInput!]) {
  productUpdate(product: $product, media: $media) {
    product { id media(first: 10) { nodes { alt mediaContentType preview { status } } } }
    userErrors { field message }
  }
}
""";

        List<String> imagesToUpload = null;

        // Process uploaded images first if provided
        if (images != null && !images.isEmpty()) {
            try {
                imagesToUpload = processUploadedImages(images);
                System.out.println("✅ Processed uploaded images: " + imagesToUpload);
            } catch (IOException e) {
                System.err.println("❌ Failed to process uploaded images: " + e.getMessage());
            }
        }

        // Fallback to request imageUrls if no uploaded images
        if (imagesToUpload == null || imagesToUpload.isEmpty()) {
            imagesToUpload = req.getImageUrls();
        }

        // Fallback to local product image if no images provided in request
        if ((imagesToUpload == null || imagesToUpload.isEmpty()) && p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
            imagesToUpload = List.of(p.getImageUrl());
        }

        ObjectNode productInput = om.createObjectNode().put("id", productGid);
        ArrayNode mediaToAdd = om.createArrayNode();
        for (String url : imagesToUpload) {
            ObjectNode m = om.createObjectNode();
            m.put("originalSource", url);
            m.put("mediaContentType", "IMAGE");
            mediaToAdd.add(m);
        }

        ObjectNode vars = om.createObjectNode();
        vars.set("product", productInput);
        vars.set("media", mediaToAdd);

        String mediaRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), updateMedia, om.writeValueAsString(vars)
        );
    }

    private boolean publishToOnlineStore(String shopDomain, String accessToken, String productGid) throws Exception {
        String onlineStorePublicationId = fetchOnlineStorePublicationId(shopDomain, accessToken);
        if (onlineStorePublicationId == null) {
            throw new IllegalStateException("Could not find 'Online Store' publication on this shop");
        }
        return publishProduct(shopDomain, accessToken, productGid, onlineStorePublicationId);
    }

    // 2) Publish a product to a specific publicationId (Online Store)
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
    
    @Deprecated
    private ResponseEntity<?> publishToShopifyInternal(Long id, PublishToShopifyRequest req, List<MultipartFile> images, List<MultipartFile> stlFiles) throws Exception {

        // Process uploaded images if any
        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            uploadedImageUrls = processUploadedImages(images);
        }

        // Process uploaded STL files if any
        if (stlFiles != null && !stlFiles.isEmpty() && stlFiles.get(0) != null && !stlFiles.get(0).isEmpty()) {
            String stlUrl = processUploadedStlFiles(id, stlFiles);
            // STL URL is stored in the product, not sent to Shopify
            System.out.println("STL file uploaded and saved to: " + stlUrl);
        } else {
            System.out.println("No STL files provided - skipping STL upload");
        }

        // Merge uploaded images with any provided image URLs
        List<String> allImageUrls = new ArrayList<>();
        if (req.getImageUrls() != null) {
            allImageUrls.addAll(req.getImageUrls());
        }
        allImageUrls.addAll(uploadedImageUrls);
        req.setImageUrls(allImageUrls);

        final PublishToShopifyRequest finalReq = req;

        // 1) Resolve Shopify account
        var acct = integrationAccountRepository.findById(finalReq.getIntegrationAccountId())
                .orElseThrow(() -> new IllegalArgumentException("integrationAccountId not found: " + finalReq.getIntegrationAccountId()));
        if (acct.getProvider() != ShopType.SHOPIFY) {
            throw new IllegalArgumentException("integrationAccountId is not SHOPIFY");
        }

        // 2) Pull your local product for defaults (adapt getters to your DTO)
        var p = productService.findById(id);
        System.out.println("🔍 Local product details: ID=" + p.getId() + ", Name=" + p.getName() + ", Price=" + p.getPrice());

        String title = coalesce(finalReq.getTitle(), p.getName());
        String descriptionHtml = coalesce(finalReq.getDescriptionHtml(), p.getDescription());
        String vendor = coalesce(finalReq.getVendor(), "YourPlatform");
        String productType = finalReq.getProductType();
        var tags = finalReq.getTags();
        String templateSuffix = finalReq.getTemplateSuffix();
        String status = coalesce(finalReq.getStatus(), "ACTIVE"); // ACTIVE or DRAFT

        // 3) productCreate (without variants - they're added separately)
        String createMutation = """
                  mutation productCreate($input: ProductInput!) {
                    productCreate(input: $input) {
                      product {
                        id
                      }
                      userErrors { field message }
                    }
                  }
                """;

        ObjectNode input = om.createObjectNode()
                .put("title", title)
                .put("descriptionHtml", descriptionHtml == null ? "" : descriptionHtml)
                .put("vendor", vendor)
                .put("status", status);

        // Remove the incorrect defaultPrice field - Shopify doesn't support this
        // Price will be set via variants instead

        if (productType != null) input.put("productType", productType);
        if (templateSuffix != null) input.put("templateSuffix", templateSuffix);

        if (tags != null && !tags.isEmpty()) {
            ArrayNode t = om.createArrayNode();
            tags.forEach(t::add);
            input.set("tags", t);
        }

        // Variants will be added separately using productVariantsBulkCreate

        // Use collections instead of productCategory
        if (req.getCollections() != null && req.getCollections().getCollectionIds() != null
                && !req.getCollections().getCollectionIds().isEmpty()) {
            ArrayNode collectionsToJoin = om.createArrayNode();
            finalReq.getCollections().getCollectionIds().forEach(collectionsToJoin::add);
            input.set("collectionsToJoin", collectionsToJoin);
        }

        ObjectNode createVars = om.createObjectNode();
        createVars.set("input", input);

        String createRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), createMutation, om.writeValueAsString(createVars)
        );

        System.out.println("📋 Product creation response: " + createRaw);

        JsonNode createJson = om.readTree(createRaw);

        // Check for GraphQL errors first
        JsonNode createGraphqlErrors = createJson.path("errors");
        if (createGraphqlErrors.isArray() && createGraphqlErrors.size() > 0) {
            System.err.println("❌ GraphQL errors in product creation: " + createGraphqlErrors.toString());
            System.err.println("❌ Full Shopify response: " + createRaw);
            return ResponseEntity.status(422).body(createRaw);
        }

        JsonNode createErrors = createJson.path("data").path("productCreate").path("userErrors");
        if (createErrors.isArray() && createErrors.size() > 0) {
            System.err.println("❌ Product creation user errors: " + createErrors.toString());
            return ResponseEntity.status(422).body(createRaw); // bubble Shopify userErrors
        }

        JsonNode productNode = createJson.path("data").path("productCreate").path("product");
        if (productNode.isMissingNode()) {
            System.err.println("❌ Product node missing from response: " + createRaw);
            return ResponseEntity.status(502).body(createRaw);
        }
        String productGid = productNode.get("id").asText();
        System.out.println("✅ Product created successfully with GID: " + productGid);

        // 3.1) Add product options if specified
        if (req.getOptions() != null && !req.getOptions().isEmpty()) {
            String optionsMutation = """
                      mutation productOptionsCreate($productId: ID!, $options: [OptionCreateInput!]!) {
                        productOptionsCreate(productId: $productId, options: $options) {
                          product { id }
                          userErrors { field message }
                        }
                      }
                    """;

            ArrayNode optionsArray = om.createArrayNode();
            for (String optionName : req.getOptions()) {
                ObjectNode option = om.createObjectNode();
                option.put("name", optionName);
                // Extract values from variants if available
                if (req.getVariants() != null && !req.getVariants().isEmpty()) {
                    ArrayNode values = om.createArrayNode();
                    for (var variant : req.getVariants()) {
                        if (variant.getOptionValues() != null && !variant.getOptionValues().isEmpty()) {
                            // For simplicity, take the first option value for each variant
                            String value = variant.getOptionValues().get(0);
                            if (!containsValue(values, value)) {
                                values.add(value);
                            }
                        }
                    }
                    option.set("values", values);
                } else {
                    // Default values if no variants specified
                    ArrayNode defaultValues = om.createArrayNode();
                    defaultValues.add("Default");
                    option.set("values", defaultValues);
                }
                optionsArray.add(option);
            }

            ObjectNode optionsVars = om.createObjectNode();
            optionsVars.put("productId", productGid);
            optionsVars.set("options", optionsArray);

            String optionsRaw = shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(), acct.getAccessToken(), optionsMutation, om.writeValueAsString(optionsVars)
            );

            JsonNode optionsJson = om.readTree(optionsRaw);
            JsonNode optionsErrors = optionsJson.path("data").path("productOptionsCreate").path("userErrors");
            if (optionsErrors.isArray() && optionsErrors.size() > 0) {
                return ResponseEntity.status(422).body(optionsRaw);
            }
        }

        // 3.2) Add product variants separately (variants field was removed from ProductInput in 2024-04+)
        String variantsMutation = """
                  mutation productCreate($input: ProductInput!) {
                            productCreate(input: $input) {
                              product {
                                id
                                title
                              }
                              userErrors {
                                field
                                message
                              }
                            }
                          }
                """;

        ObjectNode x = om.createObjectNode();
        x.put("title", "Cool socks");
        ArrayNode productOptions = om.createArrayNode().add("Title");
        x.set("options", productOptions);
        ArrayNode variants = om.createArrayNode();
        ObjectNode variant = om.createObjectNode();
        variant.put("price", "9.99");
        variant.set("options", om.createArrayNode().add("Default Title"));
        variants.add(variant);
        x.set("variants", variants);

// wrap under variables.input and serialize
        ObjectNode variables = om.createObjectNode();
        variables.set("input", x);
        String variablesJson = om.writeValueAsString(variables);


// Send 'body' as the POST body to the Shopify Admin GraphQL endpoint
//        ArrayNode variantsArray = om.createArrayNode();
//
//        if (req.getVariants() != null && !req.getVariants().isEmpty()) {
//            // Use specified variants
//            for (var v : req.getVariants()) {
//                ObjectNode vn = om.createObjectNode();
//                if (v.getOptionValues() != null && !v.getOptionValues().isEmpty()) {
//                    ArrayNode ov = om.createArrayNode();
//                    v.getOptionValues().forEach(ov::add);
//                    vn.set("optionValues", ov);
//                }
//                if (v.getBarcode() != null) vn.put("barcode", v.getBarcode());
//
//                // Price directly on variant (SKU removed - not supported in ProductVariantsBulkInput)
//                if (v.getPrice() != null && !v.getPrice().isEmpty()) {
//                    vn.put("price", v.getPrice()); // Keep as string
//                } else {
//                    vn.put("price", String.format("%.2f", p.getPrice())); // Format as string
//                }
//
//                // Ensure option values are present as key-value objects (add default if none specified)
//                if (v.getOptionValues() == null || v.getOptionValues().isEmpty()) {
//                    ArrayNode defaultOptions = om.createArrayNode();
//                    ObjectNode defaultOption = om.createObjectNode();
//                    defaultOption.put("name", "Title");
//                    defaultOption.put("value", "Default");
//                    defaultOptions.add(defaultOption);
//                    vn.set("optionValues", defaultOptions);
//                }
//
//                // SKU temporarily removed - not supported in ProductVariantsBulkInput
//                // if (v.getSku() != null) {
//                //     vn.put("sku", v.getSku());
//                // }
//
//                if (v.getCompareAtPrice() != null && !v.getCompareAtPrice().isEmpty()) {
//                    vn.put("compareAtPrice", v.getCompareAtPrice());
//                }
//
//                variantsArray.add(vn);
//            }
//        } else {
//            // Create default variant with price from local product
//            ObjectNode defaultVariant = om.createObjectNode();
//
//            // Price and default option values for variant
//            defaultVariant.put("price", String.format("%.2f", p.getPrice()));
//
//            // Add default option values as key-value objects (required by Shopify)
//            ArrayNode optionValues = om.createArrayNode();
//            ObjectNode defaultOption = om.createObjectNode();
//            defaultOption.put("name", "Title");
//            defaultOption.put("value", "Default");
//            optionValues.add(defaultOption);
//            defaultVariant.set("optionValues", optionValues);
//
//            System.out.println("💰 Creating default variant with price: $" + String.format("%.2f", p.getPrice()));
//            System.out.println("🔧 Added default option values to satisfy Shopify requirements");
//
//            variantsArray.add(defaultVariant);
//        }
//
//        ObjectNode variantsVars = om.createObjectNode();
//        variantsVars.put("productId", productGid);
//        variantsVars.set("variants", variantsArray);
//
//        // Debug: Show the exact variants data being sent
//        System.out.println("🔍 Variants payload being sent to Shopify:");
//        System.out.println("  Product ID: " + productGid);
//        System.out.println("  Variants data: " + om.writeValueAsString(variantsArray));

        String variantsRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), variantsMutation, variablesJson
        );

        // Debug: Show Shopify's response
        System.out.println("📋 Shopify variants response: " + variantsRaw);

        JsonNode variantsJson = om.readTree(variantsRaw);

        // Check for GraphQL errors first (more critical than userErrors)
        JsonNode graphqlErrors = variantsJson.path("errors");
        if (graphqlErrors.isArray() && graphqlErrors.size() > 0) {
            System.err.println("❌ GraphQL errors in variant creation: " + graphqlErrors.toString());
            System.err.println("❌ Full Shopify response: " + variantsRaw);
            return ResponseEntity.status(422).body(variantsRaw);
        }

        JsonNode variantsErrors = variantsJson.path("data").path("productVariantsBulkCreate").path("userErrors");
        if (variantsErrors.isArray() && variantsErrors.size() > 0) {
            System.err.println("❌ Variant creation user errors: " + variantsErrors.toString());
            return ResponseEntity.status(422).body(variantsRaw);
        }

        System.out.println("✅ Variants created successfully with correct price format");
        // Keep the original productNode structure - don't override it
        // The variants are created but we don't need to update productNode for inventory handling

        // 4) Process uploaded images and attach to product
        List<String> imagesToUpload = null;

        // Process uploaded images first if provided
        if (images != null && !images.isEmpty()) {
            try {
                imagesToUpload = processUploadedImages(images);
                System.out.println("✅ Processed uploaded images: " + imagesToUpload);
            } catch (IOException e) {
                System.err.println("❌ Failed to process uploaded images: " + e.getMessage());
            }
        }

        // Fallback to request imageUrls if no uploaded images
        if (imagesToUpload == null || imagesToUpload.isEmpty()) {
            imagesToUpload = req.getImageUrls();
        }

        // Fallback to local product image if no images provided in request
        if ((imagesToUpload == null || imagesToUpload.isEmpty()) && p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
            imagesToUpload = List.of(p.getImageUrl());
        }

        // Always ensure we have at least one publicly accessible image
        System.out.println("🔍 Checking images for public accessibility: " + imagesToUpload);
        boolean hasValidPublicImage = imagesToUpload != null && !imagesToUpload.isEmpty() &&
                imagesToUpload.stream().anyMatch(url -> {
                    boolean isPublic = url.startsWith("https://") && !url.contains("localhost");
                    System.out.println("🌐 URL: " + url + " -> isPublic: " + isPublic);
                    return isPublic;
                });

        if (!hasValidPublicImage) {
            System.out.println("⚠️ No valid public images found, using placeholder");
            imagesToUpload = List.of("https://via.placeholder.com/600x600/4A90E2/FFFFFF?text=3D+Print+Product");
        } else {
            System.out.println("✅ Found valid public images!");
        }

        System.out.println("📸 Images to upload to Shopify: " + imagesToUpload);

        if (imagesToUpload != null && !imagesToUpload.isEmpty()) {
            String mediaMutation = """
                      mutation productUpdateWithMedia($input: ProductInput!, $media: [CreateMediaInput!]!) {
                        productUpdate(input: $input, media: $media) {
                          product { 
                            id 
                            media(first: 10) {
                              edges {
                                node {
                                  ... on MediaImage {
                                    id
                                    image {
                                      url
                                      altText
                                    }
                                  }
                                }
                              }
                            }
                          }
                          userErrors { field message }
                        }
                      }
                    """;

            ObjectNode productInput = om.createObjectNode();
            productInput.put("id", productGid);

            ArrayNode mediaArray = om.createArrayNode();
            for (String url : imagesToUpload) {
                if (url != null && !url.isBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    ObjectNode m = om.createObjectNode();
                    m.put("originalSource", url);
                    m.put("mediaContentType", "IMAGE");
                    m.put("alt", title + " - 3D Print");
                    mediaArray.add(m);
                    System.out.println("📷 Adding image to Shopify: " + url);
                }
            }

            if (mediaArray.size() > 0) {
                ObjectNode mediaVars = om.createObjectNode();
                mediaVars.set("input", productInput);
                mediaVars.set("media", mediaArray);

                System.out.println("🚀 Sending media to Shopify...");
                String mediaRaw = shopifyGraphQLService.postGraphQL(
                        acct.getExternalShopId(), acct.getAccessToken(), mediaMutation, om.writeValueAsString(mediaVars)
                );

                System.out.println("📡 Shopify media response: " + mediaRaw);

                // Check for errors in image upload
                JsonNode mediaJson = om.readTree(mediaRaw);
                JsonNode mediaErrors = mediaJson.path("data").path("productUpdate").path("userErrors");
                if (mediaErrors.isArray() && mediaErrors.size() > 0) {
                    System.err.println("❌ Image upload errors: " + mediaErrors.toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                            "error", "Image upload failed",
                            "details", mediaErrors.toString()
                    ));
                } else {
                    System.out.println("✅ Images uploaded successfully to Shopify");
                }
            }
        }

        // 5) SEO (productUpdate with seo)
        if (req.getSeo() != null && (req.getSeo().getTitle() != null || req.getSeo().getDescription() != null)) {
            String seoMutation = """
                      mutation productUpdateSeo($input: ProductInput!) {
                        productUpdate(input: $input) {
                          product { id }
                          userErrors { field message }
                        }
                      }
                    """;
            ObjectNode seoObj = om.createObjectNode();
            if (req.getSeo().getTitle() != null) seoObj.put("title", req.getSeo().getTitle());
            if (req.getSeo().getDescription() != null) seoObj.put("description", req.getSeo().getDescription());

            ObjectNode seoInput = om.createObjectNode();
            seoInput.put("id", productGid);
            seoInput.set("seo", seoObj);

            ObjectNode seoVars = om.createObjectNode();
            seoVars.set("input", seoInput);

            shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(), acct.getAccessToken(), seoMutation, om.writeValueAsString(seoVars)
            );
        }

        // 6) Metafields (metafieldsSet)
        if (req.getMetafields() != null && !req.getMetafields().isEmpty()) {
            String mfMutation = """
                      mutation metafieldsSet($metafields: [MetafieldsSetInput!]!) {
                        metafieldsSet(metafields: $metafields) {
                          metafields { key namespace type }
                          userErrors { field message }
                        }
                      }
                    """;
            ArrayNode mfArr = om.createArrayNode();
            for (var m : req.getMetafields()) {
                ObjectNode node = om.createObjectNode();
                node.put("ownerId", productGid);
                node.put("namespace", m.getNamespace());
                node.put("key", m.getKey());
                node.put("type", m.getType());
                node.put("value", m.getValue());
                mfArr.add(node);
            }
            ObjectNode mfVars = om.createObjectNode();
            mfVars.set("metafields", mfArr);

            shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(), acct.getAccessToken(), mfMutation, om.writeValueAsString(mfVars)
            );
        }

        // 7) Inventory quantities at a single location (optional)
        if (req.getInventory() != null && req.getInventory().getLocationId() != null && req.getVariants() != null) {
            String invMutation = """
                      mutation setQty($input: InventorySetQuantitiesInput!) {
                        inventorySetQuantities(input: $input) {
                          inventoryAdjustmentGroup { createdAt reason }
                          userErrors { field message }
                        }
                      }
                    """;
            String locationId = req.getInventory().getLocationId();

            // Check if variants exist in the productNode (they should after variant creation)
            JsonNode variantsPath = productNode.path("variants");
            if (!variantsPath.isMissingNode() && variantsPath.has("edges")) {
                ArrayNode edges = (ArrayNode) variantsPath.path("edges");
                ArrayNode setQuantities = om.createArrayNode();
                for (int i = 0; i < edges.size() && i < req.getVariants().size(); i++) {
                    JsonNode variantNode = edges.get(i).path("node");
                    String inventoryItemId = variantNode.path("inventoryItem").path("id").asText();
                    Integer qty = req.getVariants().get(i).getQuantity();
                    if (qty != null && !inventoryItemId.isEmpty()) {
                        ObjectNode item = om.createObjectNode();
                        item.put("inventoryItemId", inventoryItemId);
                        item.put("locationId", locationId);
                        item.put("quantity", qty);
                        item.put("type", "on_hand");
                        setQuantities.add(item);
                    }
                }
                if (setQuantities.size() > 0) {
                    ObjectNode inputInv = om.createObjectNode();
                    inputInv.put("reason", "correction");
                    inputInv.set("setQuantities", setQuantities);

                    ObjectNode invVars = om.createObjectNode();
                    invVars.set("input", inputInv);

                    shopifyGraphQLService.postGraphQL(
                            acct.getExternalShopId(), acct.getAccessToken(), invMutation, om.writeValueAsString(invVars)
                    );
                }
            }
        }

        // Collections are now handled during product creation with collectionsToJoin

        // 8) Publish to Online Store sales channel
        System.out.println("🚀 Starting publication process...");
        try {
            // First, get available publications for this shop (now with read_publications scope)
            String publicationsQuery = """
                        query {
                          publications(first: 10) {
                            edges {
                              node {
                                id
                                name
                                supportsFuturePublishing
                              }
                            }
                          }
                        }
                    """;

            String publicationsRaw = shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(), acct.getAccessToken(), publicationsQuery, "{}"
            );

            System.out.println("📋 Publications query response: " + publicationsRaw);

            JsonNode publicationsJson = om.readTree(publicationsRaw);
            JsonNode publicationsEdges = publicationsJson.path("data").path("publications").path("edges");

            if (publicationsEdges.isMissingNode() || !publicationsEdges.isArray()) {
                System.err.println("❌ No publications data found in response");
                System.out.println("⚠️ Product created but publication failed - you may need to manually publish in Shopify admin");
            } else {
                String onlineStorePublicationId = null;
                System.out.println("📋 Searching through " + publicationsEdges.size() + " publications...");

                // Find the Online Store publication
                for (JsonNode edge : publicationsEdges) {
                    String name = edge.path("node").path("name").asText();
                    String publicationId = edge.path("node").path("id").asText();
                    System.out.println("📋 Found publication: '" + name + "' -> " + publicationId);
                    if ("Online Store".equals(name)) {
                        onlineStorePublicationId = publicationId;
                        System.out.println("✅ Found Online Store publication ID: " + publicationId);
                        break;
                    }
                }

                if (onlineStorePublicationId != null) {
                    System.out.println("📤 Publishing product " + productGid + " to Online Store...");

                    String publishMutation = """
                              mutation publishablePublish($id: ID!, $input: [PublicationInput!]!) {
                                publishablePublish(id: $id, input: $input) {
                                  publishable {
                                    publicationCount
                                  }
                                  userErrors { field message }
                                }
                              }
                            """;

                    ArrayNode publicationsArray = om.createArrayNode();
                    ObjectNode onlineStorePublication = om.createObjectNode();
                    onlineStorePublication.put("publicationId", onlineStorePublicationId);
                    publicationsArray.add(onlineStorePublication);

                    ObjectNode publishVars = om.createObjectNode();
                    publishVars.put("id", productGid);
                    publishVars.set("input", publicationsArray);

                    System.out.println("📋 Publishing with variables: " + om.writeValueAsString(publishVars));

                    String publishRaw = shopifyGraphQLService.postGraphQL(
                            acct.getExternalShopId(), acct.getAccessToken(), publishMutation, om.writeValueAsString(publishVars)
                    );

                    System.out.println("📢 Publication response: " + publishRaw);

                    JsonNode publishJson = om.readTree(publishRaw);
                    JsonNode publishErrors = publishJson.path("data").path("publishablePublish").path("userErrors");
                    if (publishErrors.isArray() && publishErrors.size() > 0) {
                        System.err.println("❌ Publication errors: " + publishErrors.toString());
                        System.out.println("⚠️ Product created but publication failed - you may need to manually publish in Shopify admin");
                    } else {
                        JsonNode publishable = publishJson.path("data").path("publishablePublish").path("publishable");
                        int publicationCount = publishable.path("publicationCount").asInt();
                        System.out.println("✅ Product published to Online Store successfully!");
                        System.out.println("📊 Publication count: " + publicationCount);

                        if (publicationCount == 0) {
                            System.out.println("⚠️ WARNING: Publication count is 0 - product may not be visible in store");
                        }
                    }
                } else {
                    System.err.println("❌ Could not find 'Online Store' publication in available publications");
                    System.out.println("⚠️ Product created but publication failed - you may need to manually publish in Shopify admin");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to publish to Online Store: " + e.getMessage());
            System.out.println("⚠️ Product created but publication failed - you may need to manually publish in Shopify admin");
            // Don't fail the entire request for publication issues
        }

        // 9) Done
        ObjectNode out = om.createObjectNode()
                .put("productGid", productGid)
                .put("status", status);
        return ResponseEntity.ok(out);
    }

    private static String coalesce(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private boolean containsValue(ArrayNode array, String value) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).asText().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private List<String> processUploadedImages(List<MultipartFile> images) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image: " + image.getOriginalFilename());
            }

            // Generate unique filename
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generate URL - use ngrok URL if available, otherwise localhost
            String baseUrl = getNgrokUrl();
            System.out.println("🔍 NGROK_URL check: " + baseUrl);
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "http://localhost:" + serverPort;
                System.out.println("📍 Using localhost URL: " + baseUrl);
            } else {
                System.out.println("🌐 Using ngrok URL: " + baseUrl);
            }
            String imageUrl = baseUrl + "/images/" + fileName;
            System.out.println("🔗 Final image URL: " + imageUrl);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    private String processUploadedStlFiles(Long productId, List<MultipartFile> stlFiles) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // For simplicity, use the first STL file
        MultipartFile stlFile = stlFiles.get(0);

        if (stlFile.isEmpty()) {
            throw new IllegalArgumentException("STL file is empty");
        }

        // Validate file type
        String originalFilename = stlFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException("File must be an STL file: " + originalFilename);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + ".stl";

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(stlFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate URL
        String baseUrl = getNgrokUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:" + serverPort;
        }
        String stlUrl = baseUrl + "/images/" + fileName; // Using same endpoint for file serving

        // Update product with STL URL
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        product.setStlFileUrl(stlUrl);
        productRepository.save(product);

        return stlUrl;
    }

    private String processUploadedStlFile(Long productId, MultipartFile stlFile) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (stlFile.isEmpty()) {
            throw new IllegalArgumentException("STL file is empty");
        }

        // Validate file type
        String originalFilename = stlFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException("File must be an STL file: " + originalFilename);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + ".stl";

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(stlFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate URL
        String baseUrl = getNgrokUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:" + serverPort;
        }
        String stlUrl = baseUrl + "/images/" + fileName; // Using same endpoint for file serving

        return stlUrl;
    }

    /**
     * Get ngrok URL from environment variable or system property.
     * Checks both NGROK_URL environment variable (set by scripts) and
     * system property (set by NgrokAutoStartService for IntelliJ debugging).
     */
    private String getNgrokUrl() {
        // First check environment variable (set by startup scripts)
        String ngrokUrl = System.getenv("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }

        // Then check system property (set by NgrokAutoStartService)
        ngrokUrl = System.getProperty("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }

        return null;
    }

    // Small DTOs for the publish endpoint
    public record PublishResult(String productGid, String variantGid, String inventoryItemGid) {
    }

}