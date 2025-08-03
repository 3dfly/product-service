package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.entity.Supplier;
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

    @InjectMocks
    private FilamentStockService filamentStockService;

    private FilamentStock testStock;
    private Supplier testSupplier;

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
                .build();
    }

    @Test
    void findAll_ShouldReturnAllStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findAll()).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
        verify(filamentStockRepository).findAll();
    }

    @Test
    void findById_WhenIdExists_ShouldReturnStock() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));

        // When
        Optional<FilamentStock> result = filamentStockService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testStock, result.get());
    }

    @Test
    void findById_WhenIdNull_ShouldReturnEmpty() {
        // When
        Optional<FilamentStock> result = filamentStockService.findById(null);

        // Then
        assertFalse(result.isPresent());
        verify(filamentStockRepository, never()).findById(any());
    }

    @Test
    void findById_WhenIdNotExists_ShouldReturnEmpty() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<FilamentStock> result = filamentStockService.findById(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_WhenValidStock_ShouldSaveAndReturn() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(filamentStockRepository.save(any(FilamentStock.class))).thenReturn(testStock);

        // When
        FilamentStock result = filamentStockService.save(testStock);

        // Then
        assertNotNull(result);
        verify(filamentStockRepository).save(testStock);
    }

    @Test
    void save_WhenStockNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.save(null));
        verify(filamentStockRepository, never()).save(any());
    }

    @Test
    void save_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.save(testStock));
    }

    @Test
    void save_WhenReservedKgNull_ShouldSetToZero() {
        // Given
        testStock.setReservedKg(null);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(filamentStockRepository.save(any(FilamentStock.class))).thenReturn(testStock);

        // When
        filamentStockService.save(testStock);

        // Then
        assertEquals(0.0, testStock.getReservedKg());
    }

    @Test
    void save_WhenNewStock_ShouldSetLastRestocked() {
        // Given
        testStock.setId(null);
        testStock.setLastRestocked(null);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(filamentStockRepository.save(any(FilamentStock.class))).thenReturn(testStock);

        // When
        filamentStockService.save(testStock);

        // Then
        assertNotNull(testStock.getLastRestocked());
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(filamentStockRepository.existsById(1L)).thenReturn(true);

        // When
        filamentStockService.deleteById(1L);

        // Then
        verify(filamentStockRepository).deleteById(1L);
    }

    @Test
    void deleteById_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.deleteById(null));
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(filamentStockRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> filamentStockService.deleteById(1L));
    }

    @Test
    void findBySupplierId_WhenValidId_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findBySupplierId(1L)).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findBySupplierId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findBySupplierId_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.findBySupplierId(null));
    }

    @Test
    void findByMaterialType_WhenValidType_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByMaterialType(FilamentType.PLA)).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findByMaterialType(FilamentType.PLA);

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findByMaterialType_WhenTypeNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.findByMaterialType(null));
    }

    @Test
    void findByColor_WhenValidColor_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByColor("Red")).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findByColor("Red");

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findByColor_WhenColorNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.findByColor(null));
    }

    @Test
    void findByColor_WhenColorEmpty_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filamentStockService.findByColor(""));
    }

    @Test
    void findAvailable_ShouldReturnAvailableStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByAvailableTrue()).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findAvailable();

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findByMaterialTypeAndColor_WhenValidParams_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findByMaterialTypeAndColor(FilamentType.PLA, "Red")).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findByMaterialTypeAndColor(FilamentType.PLA, "Red");

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findByMaterialTypeAndColor_WhenMaterialTypeNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.findByMaterialTypeAndColor(null, "Red"));
    }

    @Test
    void findByMaterialTypeAndColor_WhenColorNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.findByMaterialTypeAndColor(FilamentType.PLA, null));
    }

    @Test
    void findStockWithSufficientQuantity_WhenValidQuantity_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findStockWithSufficientQuantity(5.0)).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findStockWithSufficientQuantity(5.0);

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findStockWithSufficientQuantity_WhenQuantityNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.findStockWithSufficientQuantity(null));
    }

    @Test
    void findStockWithSufficientQuantity_WhenQuantityNegative_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.findStockWithSufficientQuantity(-1.0));
    }

    @Test
    void findLowStockItems_WhenValidThreshold_ShouldReturnStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findLowStockItems(15.0)).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findLowStockItems(15.0);

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void findLowStockItems_WhenThresholdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.findLowStockItems(null));
    }

    @Test
    void findExpiredStock_ShouldReturnExpiredStocks() {
        // Given
        List<FilamentStock> stocks = Arrays.asList(testStock);
        when(filamentStockRepository.findExpiredStock()).thenReturn(stocks);

        // When
        List<FilamentStock> result = filamentStockService.findExpiredStock();

        // Then
        assertEquals(1, result.size());
        assertEquals(testStock, result.get(0));
    }

    @Test
    void countAvailableByMaterialType_WhenValidType_ShouldReturnCount() {
        // Given
        when(filamentStockRepository.countAvailableByMaterialType(FilamentType.PLA)).thenReturn(5L);

        // When
        Long result = filamentStockService.countAvailableByMaterialType(FilamentType.PLA);

        // Then
        assertEquals(5L, result);
    }

    @Test
    void countAvailableByMaterialType_WhenTypeNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.countAvailableByMaterialType(null));
    }

    @Test
    void reserveStock_WhenValidParams_ShouldReserveAndReturn() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
        when(filamentStockRepository.save(any(FilamentStock.class))).thenReturn(testStock);

        // When
        FilamentStock result = filamentStockService.reserveStock(1L, 5.0);

        // Then
        assertNotNull(result);
        assertEquals(7.0, testStock.getReservedKg()); // 2.0 + 5.0
    }

    @Test
    void reserveStock_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.reserveStock(null, 5.0));
    }

    @Test
    void reserveStock_WhenQuantityNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.reserveStock(1L, null));
    }

    @Test
    void reserveStock_WhenQuantityZero_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.reserveStock(1L, 0.0));
    }

    @Test
    void reserveStock_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> filamentStockService.reserveStock(1L, 5.0));
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> filamentStockService.reserveStock(1L, 15.0)); // More than available
    }

    @Test
    void releaseReservedStock_WhenValidParams_ShouldReleaseAndReturn() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));
        when(filamentStockRepository.save(any(FilamentStock.class))).thenReturn(testStock);

        // When
        FilamentStock result = filamentStockService.releaseReservedStock(1L, 1.0);

        // Then
        assertNotNull(result);
        assertEquals(1.0, testStock.getReservedKg()); // 2.0 - 1.0
    }

    @Test
    void releaseReservedStock_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.releaseReservedStock(null, 1.0));
    }

    @Test
    void releaseReservedStock_WhenQuantityNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> filamentStockService.releaseReservedStock(1L, null));
    }

    @Test
    void releaseReservedStock_WhenStockNotFound_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> filamentStockService.releaseReservedStock(1L, 1.0));
    }

    @Test
    void releaseReservedStock_WhenReleasingMoreThanReserved_ShouldThrowException() {
        // Given
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> filamentStockService.releaseReservedStock(1L, 5.0)); // More than reserved
    }

    @Test
    void releaseReservedStock_WhenReservedKgNull_ShouldThrowException() {
        // Given
        testStock.setReservedKg(null);
        when(filamentStockRepository.findById(1L)).thenReturn(Optional.of(testStock));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> filamentStockService.releaseReservedStock(1L, 1.0));
    }
}