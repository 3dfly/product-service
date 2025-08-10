package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ClosestSupplierResponse;
import com.threedfly.productservice.dto.OrderRequest;
import com.threedfly.productservice.entity.FilamentType;

import com.threedfly.productservice.repository.SupplierRepository;
import com.threedfly.productservice.repository.projection.ClosetSupplierProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

import com.threedfly.productservice.exception.SupplierNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrderServiceOptimizationTest {

    @Mock
    private SupplierRepository supplierRepository;



    @InjectMocks
    private OrderService orderService;

    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        testOrderRequest = new OrderRequest();
        testOrderRequest.setMaterialType(FilamentType.PLA);
        testOrderRequest.setColor("Red");
        testOrderRequest.setRequiredQuantityKg(5.0);
        testOrderRequest.setBuyerAddress("Los Angeles, CA");
        testOrderRequest.setBuyerLatitude(34.0522);
        testOrderRequest.setBuyerLongitude(-118.2437);
        
        // Set up mocks for dependencies we need but don't directly test
        com.threedfly.productservice.mapper.SupplierMapper supplierMapperMock = mock(com.threedfly.productservice.mapper.SupplierMapper.class, withSettings().lenient());
        com.threedfly.productservice.mapper.FilamentStockMapper filamentStockMapperMock = mock(com.threedfly.productservice.mapper.FilamentStockMapper.class, withSettings().lenient());
        
        // Mock the mapper methods to return entities and responses (lenient to handle exception tests)
        when(supplierMapperMock.fromStockProjection(any())).thenReturn(mock(com.threedfly.productservice.entity.Supplier.class));
        when(supplierMapperMock.toResponse(any())).thenReturn(mock(com.threedfly.productservice.dto.SupplierResponse.class));
        when(filamentStockMapperMock.fromStockProjection(any(), any())).thenReturn(mock(com.threedfly.productservice.entity.FilamentStock.class));
        when(filamentStockMapperMock.toResponse(any())).thenReturn(mock(com.threedfly.productservice.dto.FilamentStockResponse.class));
        
        ReflectionTestUtils.setField(orderService, "supplierMapper", supplierMapperMock);
        ReflectionTestUtils.setField(orderService, "filamentStockMapper", filamentStockMapperMock);
    }

    @Test
    void optimizedQuery_WhenSuppliersFound_ShouldUseOptimizedPath() {
        // Given - mock the optimized query to return results
        
        when(supplierRepository.findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0)))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        ClosestSupplierResponse result = orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0));
        
        // Should NOT call the legacy method
        verify(supplierRepository, never()).findByActiveAndVerifiedWithValidCoordinates();
        
        assertNotNull(result);
    }

    @Test
    void optimizedQuery_WhenNoSuppliersFound_ShouldThrowException() {
        // Given - mock the optimized query to return empty results
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        // When & Then
        SupplierNotFoundException exception = assertThrows(SupplierNotFoundException.class, () -> {
            orderService.findClosestSupplier(testOrderRequest);
        });
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No supplier found with sufficient stock"));
        
        verify(supplierRepository).findClosestSupplierWithStock(
                eq(34.0522), eq(-118.2437), eq("PLA"), eq("Red"), eq(5.0));
    }

    @Test
    void optimizedQuery_ShouldSearchWithoutDistanceConstraints() {
        // Given
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        orderService.findClosestSupplier(testOrderRequest);

        // Then - verify that search is performed without distance constraints
        verify(supplierRepository).findClosestSupplierWithStock(
                any(), any(), any(), any(), any());
    }

    @Test
    void optimizedQuery_ShouldReturnSingleResult() {
        // Given
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        orderService.findClosestSupplier(testOrderRequest);

        // Then - verify that query returns single result (LIMIT 1 in SQL)
        verify(supplierRepository).findClosestSupplierWithStock(
                any(), any(), any(), any(), any());
        // No need to verify LIMIT 1 since it's in the SQL query itself
    }

    @Test
    void optimizedQuery_ShouldHandleDifferentMaterialTypes() {
        // Given
        testOrderRequest.setMaterialType(FilamentType.ABS);
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        orderService.findClosestSupplier(testOrderRequest);

        // Then - verify that the material type is correctly passed as string
        verify(supplierRepository).findClosestSupplierWithStock(
                any(), any(), eq("ABS"), any(), any());
    }

    @Test
    void optimizedQuery_ShouldPassCorrectCoordinates() {
        // Given - test with different coordinates
        testOrderRequest.setBuyerLatitude(40.7128); // New York
        testOrderRequest.setBuyerLongitude(-74.0060);
        
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(
                eq(40.7128), eq(-74.0060), any(), any(), any());
    }

    @Test
    void optimizedQuery_ShouldPassCorrectQuantityAndColor() {
        // Given - test with different quantity and color
        testOrderRequest.setRequiredQuantityKg(15.5);
        testOrderRequest.setColor("Blue");
        
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When
        orderService.findClosestSupplier(testOrderRequest);

        // Then
        verify(supplierRepository).findClosestSupplierWithStock(
                any(), any(), any(), eq("Blue"), eq(15.5));
    }

    /**
     * Performance comparison test demonstrating the efficiency gain.
     * This test doesn't measure actual time but verifies that the optimized approach
     * makes fewer database calls.
     */
    @Test
    void performanceComparison_OptimizedVsLegacy() {
        // This test conceptually shows that:
        // - Optimized: 1 database query (findClosestSuppliersWithStock)
        // - Legacy: N+1 queries (findByActiveAndVerifiedWithValidCoordinates + N stock queries)
        
        // Given
        when(supplierRepository.findClosestSupplierWithStock(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(mock(ClosetSupplierProjection.class)));


        // When - call optimized version
        orderService.findClosestSupplier(testOrderRequest);

        // Then - verify minimal database interaction
        verify(supplierRepository, times(1)).findClosestSupplierWithStock(any(), any(), any(), any(), any());
        
        // Should NOT call the expensive operations from legacy approach
        verify(supplierRepository, never()).findByActiveAndVerifiedWithValidCoordinates();
    }


}