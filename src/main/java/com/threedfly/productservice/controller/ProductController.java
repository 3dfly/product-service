package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.threedfly.productservice.dto.ProductRequest;
import com.threedfly.productservice.dto.ProductResponse;
import com.threedfly.productservice.entity.IntegrationAccount;
import com.threedfly.productservice.entity.ProductSync;
import com.threedfly.productservice.entity.ShopType;
import com.threedfly.productservice.repository.IntegrationAccountRepository;
import com.threedfly.productservice.repository.ProductSyncRepository;
import com.threedfly.productservice.service.ProductService;
import com.threedfly.shopify.dto.Metafield;
import com.threedfly.shopify.dto.PublishToShopifyRequest;
import com.threedfly.shopify.dto.Variant;
import com.threedfly.shopify.service.ShopifyGraphQLService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Validated
public class ProductController {
    private final ProductService productService;

    private final IntegrationAccountRepository integrationAccountRepository;
    private final ProductSyncRepository productSyncRepository;
    private final ShopifyGraphQLService shopifyGraphQLService;
    private final ObjectMapper om;

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

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable @NotNull Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NotNull Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish/shopify")
    public ResponseEntity<?> publishToShopify(
            @PathVariable Long id,
            @RequestBody PublishToShopifyRequest req) throws Exception {

        // 1) Resolve Shopify account
        var acct = integrationAccountRepository.findById(req.getIntegrationAccountId())
                .orElseThrow(() -> new IllegalArgumentException("integrationAccountId not found: " + req.getIntegrationAccountId()));
        if (acct.getProvider() != ShopType.SHOPIFY) {
            throw new IllegalArgumentException("integrationAccountId is not SHOPIFY");
        }

        // 2) Pull your local product for defaults (adapt getters to your DTO)
        var p = productService.findById(id);
        String title            = coalesce(req.getTitle(),           p.getName());
        String descriptionHtml  = coalesce(req.getDescriptionHtml(), p.getDescription());
        String vendor           = coalesce(req.getVendor(),          "YourPlatform");
        String productType      = req.getProductType();
        var    tags             = req.getTags();
        String templateSuffix   = req.getTemplateSuffix();
        String productCategoryId= req.getProductCategoryId();
        String status           = coalesce(req.getStatus(),          "ACTIVE"); // ACTIVE or DRAFT

        // 3) productCreate with options & variants
        String createMutation = """
      mutation productCreate($input: ProductInput!) {
        productCreate(input: $input) {
          product {
            id
            variants(first: 50) { edges { node { id sku price compareAtPrice inventoryItem { id } } } }
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

        if (productType != null)     input.put("productType", productType);
        if (templateSuffix != null)  input.put("templateSuffix", templateSuffix);
        if (productCategoryId != null) input.put("productCategory", productCategoryId); // taxonomy GID

        if (tags != null && !tags.isEmpty()) {
            ArrayNode t = om.createArrayNode();
            tags.forEach(t::add);
            input.set("tags", t);
        }

        // Option names (e.g., ["Color","Size"])
        if (req.getOptions() != null && !req.getOptions().isEmpty()) {
            ArrayNode optionNames = om.createArrayNode();
            req.getOptions().forEach(optionNames::add);
            input.set("options", optionNames);
        }

        // Variants
        ArrayNode variants = om.createArrayNode();
        if (req.getVariants() != null && !req.getVariants().isEmpty()) {
            for (var v : req.getVariants()) {
                ObjectNode vn = om.createObjectNode();
                if (v.getOptionValues() != null) {
                    ArrayNode ov = om.createArrayNode();
                    v.getOptionValues().forEach(ov::add);
                    vn.set("options", ov);
                }
                if (v.getSku() != null)            vn.put("sku", v.getSku());
                if (v.getBarcode() != null)        vn.put("barcode", v.getBarcode());
                if (v.getPrice() != null)          vn.put("price", v.getPrice());
                if (v.getCompareAtPrice() != null) vn.put("compareAtPrice", v.getCompareAtPrice());
                variants.add(vn);
            }
        } else {
            // fallback single variant from simple fields / local product
            ObjectNode vn = om.createObjectNode()
                    .put("sku", coalesce(req.getSku(), p.getSku() == null ? "" : p.getSku()))
                    .put("price", coalesce(req.getPrice(), "0.00"));
            if (req.getBarcode() != null)        vn.put("barcode", req.getBarcode());
            if (req.getCompareAtPrice() != null) vn.put("compareAtPrice", req.getCompareAtPrice());
            variants.add(vn);
        }
        input.set("variants", variants);

        ObjectNode createVars = om.createObjectNode();
        createVars.set("input", input);

        String createRaw = shopifyGraphQLService.postGraphQL(
                acct.getExternalShopId(), acct.getAccessToken(), createMutation, om.writeValueAsString(createVars)
        );

        JsonNode createJson   = om.readTree(createRaw);
        JsonNode createErrors = createJson.path("data").path("productCreate").path("userErrors");
        if (createErrors.isArray() && createErrors.size() > 0) {
            return ResponseEntity.status(422).body(createRaw); // bubble Shopify userErrors
        }

        JsonNode productNode = createJson.path("data").path("productCreate").path("product");
        if (productNode.isMissingNode()) {
            return ResponseEntity.status(502).body(createRaw);
        }
        String productGid = productNode.get("id").asText();

        // 4) Attach images via productUpdate(media) if provided
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            String mediaMutation = """
          mutation UpdateProductWithNewMedia($product: ProductUpdateInput!, $media: [CreateMediaInput!]) {
            productUpdate(product: $product, media: $media) {
              product { id }
              userErrors { field message }
            }
          }
        """;

            ObjectNode productVar = om.createObjectNode().put("id", productGid);
            ArrayNode mediaArray  = om.createArrayNode();
            for (String url : req.getImageUrls()) {
                ObjectNode m = om.createObjectNode();
                m.put("originalSource", url);
                m.put("mediaContentType", "IMAGE");
                m.put("alt", title);
                mediaArray.add(m);
            }
            ObjectNode mediaVars = om.createObjectNode();
            mediaVars.set("product", productVar);
            mediaVars.set("media",   mediaArray);

            shopifyGraphQLService.postGraphQL(
                    acct.getExternalShopId(), acct.getAccessToken(), mediaMutation, om.writeValueAsString(mediaVars)
            );
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
            if (req.getSeo().getTitle() != null)       seoObj.put("title", req.getSeo().getTitle());
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

            ArrayNode edges = (ArrayNode) productNode.path("variants").path("edges");
            ArrayNode setQuantities = om.createArrayNode();
            for (int i = 0; i < edges.size() && i < req.getVariants().size(); i++) {
                JsonNode variantNode = edges.get(i).path("node");
                String inventoryItemId = variantNode.path("inventoryItem").path("id").asText();
                Integer qty = req.getVariants().get(i).getQuantity();
                if (qty != null) {
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

        // 8) Add to manual collections (optional)
        if (req.getCollections() != null && req.getCollections().getCollectionIds() != null
                && !req.getCollections().getCollectionIds().isEmpty()) {
            String addToCollections = """
          mutation collectionAddProducts($id: ID!, $productIds: [ID!]!) {
            collectionAddProducts(id: $id, productIds: $productIds) {
              job { id }
              userErrors { field message }
            }
          }
        """;
            for (String cid : req.getCollections().getCollectionIds()) {
                ObjectNode vars = om.createObjectNode();
                vars.put("id", cid);
                ArrayNode ids = om.createArrayNode().add(productGid);
                vars.set("productIds", ids);

                shopifyGraphQLService.postGraphQL(
                        acct.getExternalShopId(), acct.getAccessToken(), addToCollections, om.writeValueAsString(vars)
                );
            }
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

    // Small DTOs for the publish endpoint
    public record PublishResult(String productGid, String variantGid, String inventoryItemGid) {}

}