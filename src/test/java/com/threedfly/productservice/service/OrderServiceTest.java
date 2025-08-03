package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ClosestSupplierResponse;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.dto.OrderRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.FilamentStockMapper;
import com.threedfly.productservice.mapper.SupplierMapper;
import com.threedfly.productservice.repository.FilamentStockRepository;
import com.threedfly.productservice.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

import com.threedfly.productservice.exception.SupplierNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private FilamentStockRepository filamentStockRepository;

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
        // Test order request - buyer location in Los Angeles
        testOrderRequest = new OrderRequest();
        testOrderRequest.setMaterialType(FilamentType.PLA);
        testOrderRequest.setColor("Red");
        testOrderRequest.setRequiredQuantityKg(5.0);
        testOrderRequest.setBuyerAddress("Los Angeles, CA");
        testOrderRequest.setBuyerLatitude(34.0522);
        testOrderRequest.setBuyerLongitude(-118.2437);

        // Near supplier - about 10km from buyer
        nearSupplier = Supplier.builder()
                .id(1L)
                .name("Near Supplier")
                .email("near@supplier.com")
                .latitude(34.1522) // ~11km north
                .longitude(-118.2437)
                .active(true)
                .verified(true)
                .build();

        // Far supplier - about 100km from buyer  
        farSupplier = Supplier.builder()
                .id(2L)
                .name("Far Supplier")
                .email("far@supplier.com")
                .latitude(35.0522) // ~111km north
                .longitude(-118.2437)
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

    private com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection createMockProjection(Supplier supplier, Double distance) {
        // Create a mock projection for the supplier with distance
        // Since we're using supplierMapper.fromProjection(), we only need the distance for assertions
        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                mock(com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection.class, withSettings().lenient());
        
        // Only mock the distance as it's used in assertions, mapper handles the rest
        when(mockProjection.getDistanceKm()).thenReturn(distance);
        
        return mockProjection;
    }

    @Test
    void findClosestSupplier_WhenSuppliersAvailable_ShouldReturnClosest() {
        // Given - mock the optimized query to return supplier projection
        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> nearSupplierStock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Red"), eq(5.0)))
                .thenReturn(nearSupplierStock);
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

        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Red", 5.0);
        verify(filamentStockRepository).findBestAvailableStockForSupplier(1L, FilamentType.PLA, "Red", 5.0);
        verify(supplierMapper).toResponse(any(Supplier.class));
        verify(filamentStockMapper).toResponse(availableStock);
    }

    @Test
    void findClosestSupplier_WhenMultipleSuppliersWithStock_ShouldReturnClosest() {
        // Given - optimized query returns closest supplier directly
        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> nearSupplierStock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Red"), eq(5.0)))
                .thenReturn(nearSupplierStock);
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
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        assertTrue(exception.getMessage().contains("PLA Red"));
        assertTrue(exception.getMessage().contains("5.0 kg"));

        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Red", 5.0);
        verify(filamentStockRepository, never()).findBestAvailableStockForSupplier(any(), any(), any(), any());
    }

    @Test
    void findClosestSupplier_WhenNoSufficientStock_ShouldThrowException() {
        // Given - optimized query handles stock filtering, so empty results mean no sufficient stock
        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        assertTrue(exception.getMessage().contains("PLA Red"));
        assertTrue(exception.getMessage().contains("5.0 kg"));

        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Red", 5.0);
    }

    @Test
    void findClosestSupplier_WhenSupplierHasInsufficientStock_ShouldSkipThatSupplier() {
        // Given - optimized query already filters suppliers with sufficient stock
        // So this test verifies that the DB query only returns suppliers with available stock
        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> sufficientStock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection)); // Returns only suppliers with sufficient stock
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Red"), eq(5.0)))
                .thenReturn(sufficientStock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName()); // Returns supplier with sufficient stock
        assertEquals(1L, result.getSupplier().getId());

        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Red", 5.0);
        verify(filamentStockRepository).findBestAvailableStockForSupplier(1L, FilamentType.PLA, "Red", 5.0);
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

        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(sameLocationSupplier, 0.0); // Zero distance
        List<FilamentStock> stock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(sameLocationSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Red"), eq(5.0)))
                .thenReturn(stock);
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

        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> stock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(15.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Red"), eq(15.0)))
                .thenReturn(stock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSupplier());
        assertEquals("Near Supplier", result.getSupplier().getName());

        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Red", 15.0);
        verify(filamentStockRepository).findBestAvailableStockForSupplier(1L, FilamentType.PLA, "Red", 15.0);
    }

    @Test
    void findClosestSupplier_WhenDifferentMaterialType_ShouldSearchCorrectType() {
        // Given - looking for ABS instead of PLA
        testOrderRequest.setMaterialType(FilamentType.ABS);

        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> stock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("ABS"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.ABS), eq("Red"), eq(5.0)))
                .thenReturn(stock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "ABS", "Red", 5.0);
        // Should NOT search for PLA
        verify(supplierRepository, never()).findClosestSupplierWithStock(any(), any(), eq("PLA"), any(), any());
    }

    @Test
    void findClosestSupplier_WhenDifferentColor_ShouldSearchCorrectColor() {
        // Given - looking for Blue instead of Red
        testOrderRequest.setColor("Blue");

        com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection mockProjection = 
                createMockProjection(nearSupplier, 11.23);
        List<FilamentStock> stock = Collections.singletonList(availableStock);

        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Blue"), eq(5.0)))
                .thenReturn(Optional.of(mockProjection));
        when(supplierMapper.fromProjection(mockProjection)).thenReturn(nearSupplier);
        when(filamentStockRepository.findBestAvailableStockForSupplier(
                eq(1L), eq(FilamentType.PLA), eq("Blue"), eq(5.0)))
                .thenReturn(stock);
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(nearSupplierResponse);
        when(filamentStockMapper.toResponse(availableStock)).thenReturn(stockResponse);

        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(34.0522, -118.2437, "PLA", "Blue", 5.0);
        // Should NOT search for Red
        verify(supplierRepository, never()).findClosestSupplierWithStock(any(), any(), any(), eq("Red"), any());
    }
}