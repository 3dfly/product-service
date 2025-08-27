package com.threedfly.productservice.controller;

import com.threedfly.shopify.service.ShopifyGraphQLService;
import dto.ProductRequest;
import dto.ProductResponse;
import dto.PublishToStoreRequest;
import entity.IntegrationAccount;
import entity.Product;
import entity.ShopType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import repository.IntegrationAccountRepository;
import repository.ProductRepository;
import service.ProductService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Validated
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final IntegrationAccountRepository integrationAccountRepository;
    private final ShopifyGraphQLService shopifyGraphQLService;

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

        // Get product entity for file storage
        Product productEntity = productRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Process and store STL file if provided
        if (stlFile != null && !stlFile.isEmpty()) {
            storeStlFileInProduct(productEntity, stlFile);
        }

        // Process and store image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            storeImageFileInProduct(productEntity, imageFile);
        }

        // Save updated product entity
        productRepository.save(productEntity);

        // Return updated product response
        product = productService.findById(product.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    // Helper method to store image file in product entity
    private void storeImageFileInProduct(Product product, MultipartFile imageFile) throws IOException {
        // Validate file type
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image: " + imageFile.getOriginalFilename());
        }

        // Store file data
        product.setImageData(imageFile.getBytes());
        product.setImageFilename(imageFile.getOriginalFilename());
        product.setImageContentType(contentType);
    }

    // Helper method to store STL file in product entity
    private void storeStlFileInProduct(Product product, MultipartFile stlFile) throws IOException {
        // Validate file type
        String originalFilename = stlFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException("File must be an STL file: " + originalFilename);
        }

        // Store file data
        product.setStlData(stlFile.getBytes());
        product.setStlFilename(originalFilename);
        product.setStlContentType(stlFile.getContentType());
    }

    // Endpoint to serve product images
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(product.getImageContentType()))
                .header("Content-Disposition", "inline; filename=\"" + product.getImageFilename() + "\"")
                .body(product.getImageData());
    }

    // Endpoint to serve product STL files
    @GetMapping("/{id}/stl")
    public ResponseEntity<byte[]> getProductStl(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStlData() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(product.getStlContentType() != null ?
                        product.getStlContentType() : "application/octet-stream"))
                .header("Content-Disposition", "attachment; filename=\"" + product.getStlFilename() + "\"")
                .body(product.getStlData());
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

        // Get product entity for file storage
        Product productEntity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Process and store STL file if provided
        if (stlFile != null && !stlFile.isEmpty()) {
            storeStlFileInProduct(productEntity, stlFile);
        }

        // Process and store image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            storeImageFileInProduct(productEntity, imageFile);
        }

        // Save updated product entity if any files were processed
        if ((stlFile != null && !stlFile.isEmpty()) || (imageFile != null && !imageFile.isEmpty())) {
            productRepository.save(productEntity);
            // Return updated product response
            product = productService.findById(id);
        }

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NotNull Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/shopify")
    public ResponseEntity<?> deleteProductFromStore(@PathVariable @NotNull Long id, @RequestBody @NotNull ShopType shopType) {
        return productService.deleteProduct(id, shopType);
    }

    @PostMapping(value = "/{id}/publish/shopify")
    public ResponseEntity<?> publishProductToStore(
            @PathVariable Long id,
            @RequestBody PublishToStoreRequest req) throws Exception {
        return productService.publishProduct(id, req);
    }
}