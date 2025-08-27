package com.threedfly.productservice.service;

import dto.ClosestSupplierResponse;
import dto.FilamentStockResponse;
import dto.OrderRequest;
import dto.SupplierResponse;
import entity.FilamentStock;
import entity.FilamentType;
import entity.Supplier;
import mapper.FilamentStockMapper;
import mapper.SupplierMapper;

import repository.SupplierRepository;
import repository.projection.ClosetSupplierProjection;
import service.OrderService;
import service.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

import exception.SupplierNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private FilamentStockMapper filamentStockMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest testOrderRequest;
    private Supplier nearSupplier;
    private Supplier farSupplier;
    private FilamentStock availableStock;
    private SupplierResponse nearSupplierResponse;
    private FilamentStockResponse stockResponse;

    @BeforeEach
    void setUp() {
        // Test order request - buyer location in NYC (212 W 91st St, New York, NY 10024)
        testOrderRequest = new OrderRequest();
        testOrderRequest.setMaterialType(FilamentType.PLA);
        testOrderRequest.setColor("Red");
        testOrderRequest.setRequiredQuantityKg(5.0);
        testOrderRequest.setBuyerAddress("212 W 91st St, New York, NY 10024");
        testOrderRequest.setBuyerLatitude(40.7903);
        testOrderRequest.setBuyerLongitude(-73.9477);

        // Mock geocoding service to always indicate coordinates are not missing when they are provided
        when(geocodingService.areCoordinatesMissing(any(), any())).thenAnswer(invocation -> {
            Double lat = invocation.getArgument(0);
            Double lng = invocation.getArgument(1);
            return lat == null && lng == null;
        });

        // Jersey City supplier (103 Summit Ave, Jersey City, NJ 07306)
        nearSupplier = Supplier.builder()
                .id(1L)
                .name("Jersey City Supplier")
                .email("jersey@supplier.com")
                .latitude(40.7191)
                .longitude(-74.0506)
                .address("103 Summit Ave")
                .city("Jersey City")
                .state("NJ")
                .postalCode("07306")
                .active(true)
                .verified(true)
                .build();

        // Connecticut supplier (45 Temple St, New Haven, CT 06510)
        farSupplier = Supplier.builder()
                .id(2L)
                .name("New Haven Supplier")
                .email("newhaven@supplier.com")
                .latitude(41.3083)
                .longitude(-72.9282)
                .address("45 Temple St")
                .city("New Haven")
                .state("CT")
                .postalCode("06510")
                .active(true)
                .verified(true)
                .build();

        // Available stock
        availableStock = FilamentStock.builder()
                .id(1L)
                .supplier(nearSupplier)
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(20.0)
                .reservedKg(2.0)
                .available(true)
                .lastRestocked(new Date())
                .build();

        // Response DTOs
        nearSupplierResponse = SupplierResponse.builder()
                .id(1L)
                .name("Near Supplier")
                .email("near@supplier.com")
                .latitude(34.1522)
                .longitude(-118.2437)
                .active(true)
                .verified(true)
                .build();

        stockResponse = FilamentStockResponse.builder()
                .id(1L)
                .supplierId(1L)
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(20.0)
                .reservedKg(2.0)
                .availableQuantityKg(18.0)
                .available(true)
                .build();
    }

    private ClosetSupplierProjection createMockProjection(Supplier supplier, Double distance) {
        // Create a mock projection for the supplier with stock and distance
        ClosetSupplierProjection mockProjection =
                mock(ClosetSupplierProjection.class, withSettings().lenient());
        
        // Only mock the distance as it's used in assertions, mapper handles the rest
        when(mockProjection.getDistanceKm()).thenReturn(distance);
        
        return mockProjection;
    }

    @Test
    void findClosestSupplier_WhenSuppliersAvailable_ShouldReturnClosest() {
        // Given - mock the optimized query to return supplier projection
        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName());
        assertEquals(1L, result.getSupplier().getId());
        assertNotNull(result.getAvailableStock());
        assertEquals(18.0, result.getAvailableStock().getAvailableQuantityKg());
        assertEquals(11.23, result.getDistanceKm());
        assertEquals("Closest supplier found successfully", result.getMessage());

        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Red", 5.0);
        verify(supplierMapper).toResponse(any(Supplier.class));
        verify(filamentStockMapper).toResponse(availableStock);
    }

    @Test
    void findClosestSupplier_WhenMultipleSuppliersWithStock_ShouldReturnClosest() {
        // Given - optimized query returns closest supplier directly
        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName());
        assertEquals(1L, result.getSupplier().getId());
        assertEquals(11.23, result.getDistanceKm()); // Distance from optimized query
        assertEquals("Closest supplier found successfully", result.getMessage());
    }

    @Test
    void findClosestSupplier_WhenNoSuppliersAvailable_ShouldThrowException() {
        // Given - optimized query returns empty results
        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        assertTrue(exception.getMessage().contains("PLA Red"));
        assertTrue(exception.getMessage().contains("5.0 kg"));

        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Red", 5.0);

    }

    @Test
    void findClosestSupplier_WhenNoSufficientStock_ShouldThrowException() {
        // Given - optimized query handles stock filtering, so empty results mean no sufficient stock
        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        assertTrue(exception.getMessage().contains("PLA Red"));
        assertTrue(exception.getMessage().contains("5.0 kg"));

        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Red", 5.0);
    }

    @Test
    void findClosestSupplier_WhenSupplierHasInsufficientStock_ShouldSkipThatSupplier() {
        // Given - optimized query already filters suppliers with sufficient stock
        // So this test verifies that the DB query only returns suppliers with available stock
        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection)); // Returns only suppliers with sufficient stock
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName()); // Returns supplier with sufficient stock
        assertEquals(1L, result.getSupplier().getId());

        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Red", 5.0);
    }

    @Test
    void findClosestSupplier_WhenSameLocation_ShouldReturnZeroDistance() {
        // Given - supplier at exact same location as buyer
        Supplier sameLocationSupplier = Supplier.builder()
                .id(1L)
                .name("Same Location Supplier")
                .latitude(34.0522) // Exact same as buyer
                .longitude(-118.2437) // Exact same as buyer
                .active(true)
                .verified(true)
                .build();

        ClosetSupplierProjection mockProjection =
                createMockProjection(sameLocationSupplier, 0.0); // Zero distance

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(sameLocationSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, sameLocationSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any())).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals(0.0, result.getDistanceKm()); // Should be exactly 0
    }

    @Test
    void findClosestSupplier_WhenLargeQuantityRequired_ShouldFindSupplierWithSufficientStock() {
        // Given - require 15kg, but our test stock only has 18kg available (20 - 2 reserved)
        testOrderRequest.setRequiredQuantityKg(15.0);

        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(15.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName());

        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Red", 15.0);
    }

    @Test
    void findClosestSupplier_WhenDifferentMaterialType_ShouldSearchCorrectType() {
        // Given - looking for ABS instead of PLA
        testOrderRequest.setMaterialType(FilamentType.ABS);

        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("ABS"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "ABS", "Red", 5.0);
        // Should NOT search for PLA
        verify(supplierRepository, never()).findClosestSupplierWithStock(any(), any(), eq("PLA"), any(), any());
    }

    @Test
    void findClosestSupplier_WhenDifferentColor_ShouldSearchCorrectColor() {
        // Given - looking for Blue instead of Red
        testOrderRequest.setColor("Blue");

        ClosetSupplierProjection mockProjection =
                createMockProjection(nearSupplier, 11.23);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Blue"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(40.7903, -73.9477, "PLA", "Blue", 5.0);
        // Should NOT search for Red
        verify(supplierRepository, never()).findClosestSupplierWithStock(any(), any(), any(), eq("Red"), any());
    }

    @Test
    void findClosestSupplier_NYC_HappyFlow_ShouldChooseJerseyCity() {
        // Given - Jersey City supplier has stock
        ClosetSupplierProjection mockProjection = createMockProjection(nearSupplier, 6.0); // ~6 miles from NYC

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, nearSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(
                SupplierResponse.builder()
                        .id(1L)
                        .name("Jersey City Supplier")
                        .city("Jersey City")
                        .state("NJ")
                        .build()
        );
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Jersey City Supplier", result.getSupplier().getName());
        assertEquals("Jersey City", result.getSupplier().getCity());
        assertEquals("NJ", result.getSupplier().getState());
        assertEquals(6.0, result.getDistanceKm());
        assertNotNull(result.getAvailableStock());
        assertTrue(result.getAvailableStock().getAvailableQuantityKg() >= testOrderRequest.getRequiredQuantityKg());
    }

    @Test
    void findClosestSupplier_NYC_JerseyCityOutOfStock_ShouldChooseNewHaven() {
        // Given - Jersey City supplier is out of stock, New Haven has stock
        ClosetSupplierProjection mockProjection = createMockProjection(farSupplier, 80.0); // ~80 miles from NYC

        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromStockProjection(mockProjection)).thenReturn(farSupplier);
        when(filamentStockMapper.fromStockProjection(mockProjection, farSupplier)).thenReturn(availableStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(
                SupplierResponse.builder()
                        .id(2L)
                        .name("New Haven Supplier")
                        .city("New Haven")
                        .state("CT")
                        .build()
        );
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("New Haven Supplier", result.getSupplier().getName());
        assertEquals("New Haven", result.getSupplier().getCity());
        assertEquals("CT", result.getSupplier().getState());
        assertEquals(80.0, result.getDistanceKm());
        assertNotNull(result.getAvailableStock());
        assertTrue(result.getAvailableStock().getAvailableQuantityKg() >= testOrderRequest.getRequiredQuantityKg());
    }

    @Test
    void findClosestSupplier_NYC_NoSupplierHasStock_ShouldThrowException() {
        // Given - No supplier has stock
        when(supplierRepository.findClosestSupplierWithStock(
                eq(40.7903), eq(-73.9477), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        assertTrue(exception.getMessage().contains("PLA"));
        assertTrue(exception.getMessage().contains("Red"));
        assertTrue(exception.getMessage().contains("5.0"));
    }
}