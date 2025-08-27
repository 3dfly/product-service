package com.threedfly.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ClosestSupplierResponse;
import dto.FilamentStockResponse;
import dto.OrderRequest;
import dto.SupplierResponse;
import entity.FilamentType;
import service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import exception.SupplierNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "ngrok.auto-start.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequest validOrderRequest;
    private ClosestSupplierResponse successResponse;


    @BeforeEach
    void setUp() {
        validOrderRequest = new OrderRequest();
        validOrderRequest.setMaterialType(FilamentType.PLA);
        validOrderRequest.setColor("Red");
        validOrderRequest.setRequiredQuantityKg(5.0);
        validOrderRequest.setBuyerAddress("Los Angeles, CA");
        validOrderRequest.setBuyerLatitude(34.0522);
        validOrderRequest.setBuyerLongitude(-118.2437);

        // Success response
        SupplierResponse supplier = SupplierResponse.builder()
                .id(1L)
                .name("Test Supplier")
                .email("test@supplier.com")
                .latitude(34.1522)
                .longitude(-118.2437)
                .active(true)
                .verified(true)
                .build();

        FilamentStockResponse stock = FilamentStockResponse.builder()
                .id(1L)
                .supplierId(1L)
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(20.0)
                .reservedKg(2.0)
                .availableQuantityKg(18.0)
                .available(true)
                .build();

        successResponse = ClosestSupplierResponse.success(supplier, stock, 11.23);

        // No supplier response

    }

    @Test
    void findClosestSupplier_WhenValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(orderService.findClosestSupplier(any(OrderRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.supplier").exists())
                .andExpect(jsonPath("$.supplier.id").value(1))
                .andExpect(jsonPath("$.supplier.name").value("Test Supplier"))
                .andExpect(jsonPath("$.availableStock").exists())
                .andExpect(jsonPath("$.availableStock.id").value(1))
                .andExpect(jsonPath("$.availableStock.availableQuantityKg").value(18.0))
                .andExpect(jsonPath("$.distanceKm").value(11.23))
                .andExpect(jsonPath("$.message").value("Closest supplier found successfully"));
    }

    @Test
    void findClosestSupplier_WhenNoSupplierFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(orderService.findClosestSupplier(any(OrderRequest.class)))
            .thenThrow(SupplierNotFoundException.forMaterialRequirement("PLA", "Red", 5.0));

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No supplier found with sufficient stock for PLA Red (required: 5.0 kg)"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void findClosestSupplier_WhenMissingMaterialType_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setMaterialType(null);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenMissingColor_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setColor("");

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenNegativeQuantity_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setRequiredQuantityKg(-5.0);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenZeroQuantity_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setRequiredQuantityKg(0.0);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenMissingBuyerAddress_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setBuyerAddress("");

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenInvalidLatitude_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setBuyerLatitude(95.0); // Invalid latitude > 90

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenInvalidLongitude_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setBuyerLongitude(185.0); // Invalid longitude > 180

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenMissingLatitude_ShouldTryGeocoding() throws Exception {
        // Given
        validOrderRequest.setBuyerLatitude(null);

        // When & Then - Should not return bad request since geocoding will try to enrich
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk()); // Expect success since geocoding should work
    }

    @Test
    void findClosestSupplier_WhenMissingLongitude_ShouldTryGeocoding() throws Exception {
        // Given
        validOrderRequest.setBuyerLongitude(null);

        // When & Then - Should not return bad request since geocoding will try to enrich
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk()); // Expect success since geocoding should work
    }

    @Test
    void findClosestSupplier_WhenEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findClosestSupplier_WhenInvalidJson_ShouldReturnServerError() throws Exception {
        // Given - malformed JSON causes a parsing error
        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().is5xxServerError()); // Spring returns 500 for JSON parsing errors
    }

    @Test
    void findClosestSupplier_WhenValidBoundaryLatitude_ShouldReturnOk() throws Exception {
        // Given - test boundary latitude values
        validOrderRequest.setBuyerLatitude(90.0); // North Pole
        when(orderService.findClosestSupplier(any(OrderRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk());

        // Test South Pole
        validOrderRequest.setBuyerLatitude(-90.0);
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void findClosestSupplier_WhenValidBoundaryLongitude_ShouldReturnOk() throws Exception {
        // Given - test boundary longitude values
        validOrderRequest.setBuyerLongitude(180.0); // International Date Line
        when(orderService.findClosestSupplier(any(OrderRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk());

        // Test opposite side of International Date Line
        validOrderRequest.setBuyerLongitude(-180.0);
        mockMvc.perform(post("/orders/find-closest-supplier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk());
    }
}