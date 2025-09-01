package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.threedfly.shopify.service.ShopifyGraphQLService;
import entity.IntegrationAccount;
import entity.ShopType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.IntegrationAccountRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/billing-test")
@AllArgsConstructor
@Slf4j
public class BillingTestController {

    private final ShopifyGraphQLService shopifyGraphQLService;
    private final IntegrationAccountRepository integrationAccountRepository;
    private final ObjectMapper objectMapper;

    /**
     * Test creating a usage-based subscription
     */
    @PostMapping("/create-subscription")
    public ResponseEntity<Map<String, Object>> testCreateSubscription(
            @RequestParam String shopDomain,
            @RequestParam(defaultValue = "Test Billing Subscription") String subscriptionName,
            @RequestParam(defaultValue = "100.00") BigDecimal cappedAmount) {
        
        try {
            log.info("Testing subscription creation for shop: {}", shopDomain);

            // Find integration account
            Optional<IntegrationAccount> accountOpt = integrationAccountRepository
                .findAll().stream()
                .filter(acc ->  acc.getProvider() == ShopType.SHOPIFY &&
                              acc.getExternalShopId().equals(shopDomain))
                .findFirst();
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No integration account found for shop: " + shopDomain,
                    "suggestion", "Make sure the shop is connected via OAuth first"
                ));
            }

            IntegrationAccount account = accountOpt.get();

            String createSubscriptionMutation = """
                mutation AppSubscriptionCreate($lineItems: [AppSubscriptionLineItemInput!]!, $name: String!, $returnUrl: URL!, $test: Boolean) {
                  appSubscriptionCreate(lineItems: $lineItems, name: $name, returnUrl: $returnUrl, test: $test) {
                    appSubscription {
                      id
                      name
                      status
                      createdAt
                      lineItems {
                        id
                        plan {
                          pricingDetails {
                            ... on AppUsagePricing {
                              cappedAmount {
                                amount
                                currencyCode
                              }
                              terms
                            }
                          }
                        }
                      }
                    }
                    confirmationUrl
                    userErrors {
                      field
                      message
                    }
                  }
                }
                """;

            // Build variables
            ObjectNode variables = objectMapper.createObjectNode();
            variables.put("name", subscriptionName);
            variables.put("returnUrl", "http://localhost:8081/billing-test/confirm");
            variables.put("test", true);

            // Create line items array
            var lineItemsArray = objectMapper.createArrayNode();
            var lineItem = objectMapper.createObjectNode();
            
            var plan = objectMapper.createObjectNode();
            var pricingDetails = objectMapper.createObjectNode();
            var cappedAmountObj = objectMapper.createObjectNode();
            cappedAmountObj.put("amount", cappedAmount);
            cappedAmountObj.put("currencyCode", "USD");
            
            pricingDetails.set("cappedAmount", cappedAmountObj);
            pricingDetails.put("terms", "Pay-per-order platform fee");
            
            plan.set("appUsagePricingDetails", pricingDetails);
            lineItem.set("plan", plan);
            lineItemsArray.add(lineItem);
            
            variables.set("lineItems", lineItemsArray);

            String response = shopifyGraphQLService.postGraphQL(
                account.getExternalShopId(),
                account.getAccessToken(),
                createSubscriptionMutation,
                objectMapper.writeValueAsString(variables)
            );

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode subscription = responseJson.path("data").path("appSubscriptionCreate").path("appSubscription");
            String confirmationUrl = responseJson.path("data").path("appSubscriptionCreate").path("confirmationUrl").asText();
            JsonNode userErrors = responseJson.path("data").path("appSubscriptionCreate").path("userErrors");
            
            if (subscription.isMissingNode() || userErrors.size() > 0) {
                log.error("Failed to create subscription: {}", userErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to create subscription",
                    "details", userErrors.toString(),
                    "fullResponse", responseJson
                ));
            }

            String appSubscriptionId = subscription.path("id").asText();
            log.info("Created subscription with ID: {} and confirmation URL: {}", appSubscriptionId, confirmationUrl);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription created successfully",
                "appSubscriptionId", appSubscriptionId,
                "confirmationUrl", confirmationUrl,
                "cappedAmount", cappedAmount,
                "shopDomain", shopDomain,
                "status", subscription.path("status").asText(),
                "instructions", "Visit the confirmationUrl to approve the subscription"
            ));

        } catch (Exception e) {
            log.error("Error creating test subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to create subscription: " + e.getMessage(),
                "details", e.toString()
            ));
        }
    }

    /**
     * Cancel an active subscription
     */
    @PostMapping("/cancel-subscription")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @RequestParam String appSubscriptionId,
            @RequestParam String shopDomain) {
        
        try {
            log.info("Canceling subscription: {} for shop: {}", appSubscriptionId, shopDomain);

            // Find integration account
            Optional<IntegrationAccount> accountOpt = integrationAccountRepository
                .findAll().stream()
                .filter(acc -> acc.getProvider() == ShopType.SHOPIFY && 
                              acc.getExternalShopId().equals(shopDomain))
                .findFirst();
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No integration account found for shop: " + shopDomain
                ));
            }

            IntegrationAccount account = accountOpt.get();

            String cancelSubscriptionMutation = """
                mutation AppSubscriptionCancel($id: ID!) {
                  appSubscriptionCancel(id: $id) {
                    appSubscription {
                      id
                      name
                      status
                      createdAt
                    }
                    userErrors {
                      field
                      message
                    }
                  }
                }
                """;

            ObjectNode variables = objectMapper.createObjectNode();
            variables.put("id", appSubscriptionId);

            String response = shopifyGraphQLService.postGraphQL(
                account.getExternalShopId(),
                account.getAccessToken(),
                cancelSubscriptionMutation,
                objectMapper.writeValueAsString(variables)
            );

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode subscription = responseJson.path("data").path("appSubscriptionCancel").path("appSubscription");
            JsonNode userErrors = responseJson.path("data").path("appSubscriptionCancel").path("userErrors");
            
            if (subscription.isMissingNode() || userErrors.size() > 0) {
                log.error("Failed to cancel subscription: {}", userErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to cancel subscription",
                    "details", userErrors.toString(),
                    "fullResponse", responseJson
                ));
            }

            String status = subscription.path("status").asText();
            log.info("Successfully canceled subscription: {} - Status: {}", appSubscriptionId, status);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription canceled successfully",
                "appSubscriptionId", appSubscriptionId,
                "status", status,
                "shopDomain", shopDomain
            ));

        } catch (Exception e) {
            log.error("Error canceling subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to cancel subscription: " + e.getMessage()
            ));
        }
    }

    /**
     * Test creating a usage record (charge)
     */
    @PostMapping("/create-usage-record")
    public ResponseEntity<Map<String, Object>> testCreateUsageRecord(
            @RequestParam String subscriptionLineItemId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam String shopDomain) {
        
        try {
            log.info("Testing usage record creation: amount={}, description={}", amount, description);

            // Find integration account
            Optional<IntegrationAccount> accountOpt = integrationAccountRepository
                .findAll().stream()
                .filter(acc -> acc.getProvider() == ShopType.SHOPIFY && 
                              acc.getExternalShopId().equals(shopDomain))
                .findFirst();
            
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No integration account found for shop: " + shopDomain
                ));
            }

            IntegrationAccount account = accountOpt.get();

            String createUsageRecordMutation = """
                mutation AppUsageRecordCreate($subscriptionLineItemId: ID!, $price: MoneyInput!, $description: String!) {
                  appUsageRecordCreate(subscriptionLineItemId: $subscriptionLineItemId, price: $price, description: $description) {
                    appUsageRecord {
                      id
                      subscriptionLineItem {
                        id
                      }
                      price {
                        amount
                        currencyCode
                      }
                      description
                      createdAt
                    }
                    userErrors {
                      field
                      message
                    }
                  }
                }
                """;

            ObjectNode variables = objectMapper.createObjectNode();
            variables.put("subscriptionLineItemId", subscriptionLineItemId);
            variables.put("description", description);
            
            ObjectNode price = objectMapper.createObjectNode();
            price.put("amount", amount);
            price.put("currencyCode", "USD");
            variables.set("price", price);

            String response = shopifyGraphQLService.postGraphQL(
                account.getExternalShopId(),
                account.getAccessToken(),
                createUsageRecordMutation,
                objectMapper.writeValueAsString(variables)
            );

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode usageRecord = responseJson.path("data").path("appUsageRecordCreate").path("appUsageRecord");
            JsonNode userErrors = responseJson.path("data").path("appUsageRecordCreate").path("userErrors");
            
            if (usageRecord.isMissingNode() || userErrors.size() > 0) {
                log.error("Failed to create usage record: {}", userErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to create usage record",
                    "details", userErrors.toString(),
                    "fullResponse", responseJson
                ));
            }

            String usageRecordId = usageRecord.path("id").asText();
            log.info("Created usage record with ID: {}", usageRecordId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usage record created successfully",
                "usageRecordId", usageRecordId,
                "amount", amount,
                "description", description
            ));

        } catch (Exception e) {
            log.error("Error creating usage record", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to create usage record: " + e.getMessage()
            ));
        }
    }

    /**
     * Get connected shops for testing
     */
    @GetMapping("/connected-shops")
    public ResponseEntity<Map<String, Object>> getConnectedShops() {
        try {
            var shopifyAccounts = integrationAccountRepository.findAll().stream()
                .filter(acc -> acc.getProvider() == ShopType.SHOPIFY)
                .map(acc -> Map.of(
                    "id", acc.getId(),
                    "shopDomain", acc.getExternalShopId(),
                    "scopes", acc.getScopes() != null ? acc.getScopes() : "",
                    "installedAt", acc.getInstalledAt() != null ? acc.getInstalledAt().toString() : ""
                ))
                .toList();

            return ResponseEntity.ok(Map.of(
                "connectedShops", shopifyAccounts,
                "totalShops", shopifyAccounts.size()
            ));

        } catch (Exception e) {
            log.error("Error getting connected shops", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to get connected shops: " + e.getMessage()
            ));
        }
    }

    /**
     * Confirmation page for subscription approval
     */
    @GetMapping("/confirm")
    public String confirmSubscription() {
        return """
            <html><body>
            <h2>✅ Subscription Confirmation</h2>
            <p>Your billing subscription has been approved!</p>
            <p><a href="/billing-test.html">Back to Billing Test Interface</a></p>
            </body></html>
            """;
    }
}

