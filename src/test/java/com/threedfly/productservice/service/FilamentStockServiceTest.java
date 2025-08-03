package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.FilamentStockRequest;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.FilamentStockMapper;
import com.threedfly.productservice.repository.FilamentStockRepository;
import com.threedfly.productservice.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilamentStockServiceTest {

    @Mock
    private FilamentStockRepository filamentStockRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private FilamentStockMapper filamentStockMapper;

    @InjectMocks
    private FilamentStockService filamentStockService;

    private FilamentStock testStock;
    private Supplier testSupplier;
    private FilamentStockRequest testStockRequest;
    private FilamentStockResponse testStockResponse;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .email("test@supplier.com")
                .verified(true)
                .active(true)
                .build();

        testStock = FilamentStock.builder()
                .id(1L)
                .supplier(testSupplier)
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(10.0)
                .reservedKg(2.0)
                .available(true)
                .lastRestocked(new Date())
                .expiryDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .build();

        testStockRequest = FilamentStockRequest.builder()
                .supplierId(1L)
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(10.0)
                .reservedKg(2.0)
                .available(true)
                .lastRestocked(new Date())
                .expiryDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .build();

        testStockResponse = FilamentStockResponse.builder()
                .id(1L)
                .supplierId(1L)
                .supplierName("Test Supplier")
                .materialType(FilamentType.PLA)
                .color("Red")
                .quantityKg(10.0)
                .reservedKg(2.0)
                .available(true)
                .lastRestocked(new Date())
                .expiryDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .availableQuantityKg(8.0)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllFilamentStock() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findAll()).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testStockResponse.getColor(), result.get(0).getColor());
        assertEquals(testStockResponse.getMaterialType(), result.get(0).getMaterialType());
        verify(filamentStockRepository).findAll();
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findById_WhenIdExists_ShouldReturnFilamentStock() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        FilamentStockResponse result = filamentStockService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testStockResponse.getColor(), result.getColor());
        assertEquals(testStockResponse.getMaterialType(), result.getMaterialType());
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.findById(1L));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockMapper, never()).toResponse(any());
    }

    @Test
    void save_WhenValidStock_ShouldSaveAndReturn() {
        // Given
        when(filamentStockMapper.toEntity(testStockRequest)).thenReturn(testStock);
        when(supplierRepository.findById(testStockRequest.getSupplierId())).thenReturn(Optional.of(testSupplier));
        when(filamentStockRepository.save(testStock)).thenReturn(testStock);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        FilamentStockResponse result = filamentStockService.save(testStockRequest);

        // Then
        assertNotNull(result);
        assertEquals(testStockResponse.getColor(), result.getColor());
        assertEquals(testStockResponse.getMaterialType(), result.getMaterialType());
        verify(filamentStockMapper).toEntity(testStockRequest);
        verify(supplierRepository).findById(testStockRequest.getSupplierId());
        verify(filamentStockRepository).save(testStock);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void save_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(filamentStockMapper.toEntity(testStockRequest)).thenReturn(testStock);
        when(supplierRepository.findById(testStockRequest.getSupplierId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.save(testStockRequest));
        verify(filamentStockMapper).toEntity(testStockRequest);
        verify(supplierRepository).findById(testStockRequest.getSupplierId());
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));

        // When
        filamentStockService.deleteById(1L);

        // Then
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository).delete(testStock);
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.deleteById(1L));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository, never()).delete(any());
    }

    @Test
    void findBySupplierId_WhenValidId_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findBySupplierId(1L)).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findBySupplierId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testStockResponse.getSupplierId(), result.get(0).getSupplierId());
        verify(filamentStockRepository).findBySupplierId(1L);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findByMaterialType_WhenValidType_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByMaterialType(FilamentType.PLA)).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findByMaterialType(FilamentType.PLA);

        // Then
        assertEquals(1, result.size());
        assertEquals(FilamentType.PLA, result.get(0).getMaterialType());
        verify(filamentStockRepository).findByMaterialType(FilamentType.PLA);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findByColor_WhenValidColor_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByColor("Red")).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findByColor("Red");

        // Then
        assertEquals(1, result.size());
        assertEquals("Red", result.get(0).getColor());
        verify(filamentStockRepository).findByColor("Red");
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findAvailable_ShouldReturnAvailableStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByAvailableTrue()).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findAvailable();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
        verify(filamentStockRepository).findByAvailableTrue();
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findByMaterialTypeAndColor_WhenValidParams_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByMaterialTypeAndColor(FilamentType.PLA, "Red")).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findByMaterialTypeAndColor(FilamentType.PLA, "Red");

        // Then
        assertEquals(1, result.size());
        assertEquals(FilamentType.PLA, result.get(0).getMaterialType());
        assertEquals("Red", result.get(0).getColor());
        verify(filamentStockRepository).findByMaterialTypeAndColor(FilamentType.PLA, "Red");
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findStockWithSufficientQuantity_WhenValidQuantity_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findStockWithSufficientQuantity(5.0)).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findStockWithSufficientQuantity(5.0);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAvailableQuantityKg() >= 5.0);
        verify(filamentStockRepository).findStockWithSufficientQuantity(5.0);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findLowStockItems_WhenValidThreshold_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findLowStockItems(15.0)).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findLowStockItems(15.0);

        // Then
        assertEquals(1, result.size());
        verify(filamentStockRepository).findLowStockItems(15.0);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void findExpiredStock_ShouldReturnExpiredStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findExpiredStock()).thenReturn(stocks);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        List<FilamentStockResponse> result = filamentStockService.findExpiredStock();

        // Then
        assertEquals(1, result.size());
        verify(filamentStockRepository).findExpiredStock();
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void reserveStock_WhenValidParams_ShouldReserveAndReturn() {
        // Given
        FilamentStock mockStock = mock(FilamentStock.class);
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(mockStock));
        when(mockStock.hasEnoughStock(5.0)).thenReturn(true);
        when(mockStock.getReservedKg()).thenReturn(2.0);
        when(filamentStockRepository.save(mockStock)).thenReturn(mockStock);
        when(filamentStockMapper.toResponse(mockStock)).thenReturn(testStockResponse);

        // When
        FilamentStockResponse result = filamentStockService.reserveStock(1L, 5.0);

        // Then
        assertNotNull(result);
        verify(filamentStockRepository).findById(1L);
        verify(mockStock).setReservedKg(7.0); // 2.0 + 5.0
        verify(filamentStockRepository).save(mockStock);
        verify(filamentStockMapper).toResponse(mockStock);
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldThrowException() {
        // Given
        FilamentStock mockStock = mock(FilamentStock.class);
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(mockStock));
        when(mockStock.hasEnoughStock(15.0)).thenReturn(false);
        when(mockStock.getAvailableQuantityKg()).thenReturn(8.0);

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.reserveStock(1L, 15.0));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void reserveStock_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.reserveStock(1L, 5.0));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void releaseReservedStock_WhenValidParams_ShouldReleaseAndReturn() {
        // Given
        FilamentStock mockStock = mock(FilamentStock.class);
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(mockStock));
        when(mockStock.getReservedKg()).thenReturn(5.0);
        when(filamentStockRepository.save(mockStock)).thenReturn(mockStock);
        when(filamentStockMapper.toResponse(mockStock)).thenReturn(testStockResponse);

        // When
        FilamentStockResponse result = filamentStockService.releaseReservedStock(1L, 1.0);

        // Then
        assertNotNull(result);
        verify(filamentStockRepository).findById(1L);
        verify(mockStock).setReservedKg(4.0); // 5.0 - 1.0
        verify(filamentStockRepository).save(mockStock);
        verify(filamentStockMapper).toResponse(mockStock);
    }

    @Test
    void releaseReservedStock_WhenInsufficientReserved_ShouldThrowException() {
        // Given
        FilamentStock mockStock = mock(FilamentStock.class);
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(mockStock));
        when(mockStock.getReservedKg()).thenReturn(1.0);

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.releaseReservedStock(1L, 5.0));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void releaseReservedStock_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.releaseReservedStock(1L, 1.0));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void update_WhenValidParams_ShouldUpdateAndReturn() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
        when(supplierRepository.findById(testStockRequest.getSupplierId())).thenReturn(Optional.of(testSupplier));
        when(filamentStockRepository.save(testStock)).thenReturn(testStock);
        when(filamentStockMapper.toResponse(testStock)).thenReturn(testStockResponse);

        // When
        FilamentStockResponse result = filamentStockService.update(1L, testStockRequest);

        // Then
        assertNotNull(result);
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockMapper).updateEntityFromRequest(testStock, testStockRequest);
        verify(supplierRepository).findById(testStockRequest.getSupplierId());
        verify(filamentStockRepository).save(testStock);
        verify(filamentStockMapper).toResponse(testStock);
    }

    @Test
    void update_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.update(1L, testStockRequest));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockMapper, never()).updateEntityFromRequest(any(), any());
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void update_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
        when(supplierRepository.findById(testStockRequest.getSupplierId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.update(1L, testStockRequest));
        verify(filamentStockRepository).findById(1L);
        verify(filamentStockMapper).updateEntityFromRequest(testStock, testStockRequest);
        verify(supplierRepository).findById(testStockRequest.getSupplierId());
        verify(filamentStockRepository, never()).save(any());
    }
}