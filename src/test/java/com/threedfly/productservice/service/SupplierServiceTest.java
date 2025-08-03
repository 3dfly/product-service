package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .userId(100L)
                .name("Test Supplier")
                .email("test@supplier.com")
                .phone("+1234567890")
                .address("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("12345")
                .latitude(40.7128)
                .longitude(-74.0060)
                .businessLicense("BL123456")
                .description("Test supplier description")
                .verified(true)
                .active(true)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findAll()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
        verify(supplierRepository).findAll();
    }

    @Test
    void findById_WhenIdExists_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When
        Optional<Supplier> result = supplierService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier, result.get());
    }

    @Test
    void findById_WhenIdNull_ShouldReturnEmpty() {
        // When
        Optional<Supplier> result = supplierService.findById(null);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, never()).findById(any());
    }

    @Test
    void findById_WhenIdNotExists_ShouldReturnEmpty() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Supplier> result = supplierService.findById(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_WhenValidSupplier_ShouldSaveAndReturn() {
        // Given
        testSupplier.setId(null); // Make it a new supplier
        when(supplierRepository.existsByEmail(testSupplier.getEmail())).thenReturn(false);
        when(supplierRepository.existsByUserId(testSupplier.getUserId())).thenReturn(false);
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        // When
        Supplier result = supplierService.save(testSupplier);

        // Then
        assertNotNull(result);
        assertEquals(testSupplier, result);
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void save_WhenSupplierNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.save(null));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void save_WhenNameNull_ShouldThrowException() {
        // Given
        testSupplier.setName(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenNameEmpty_ShouldThrowException() {
        // Given
        testSupplier.setName("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenEmailNull_ShouldThrowException() {
        // Given
        testSupplier.setEmail(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenEmailEmpty_ShouldThrowException() {
        // Given
        testSupplier.setEmail("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenEmailAlreadyExists_ShouldThrowException() {
        // Given
        testSupplier.setId(null); // Make it a new supplier
        when(supplierRepository.existsByEmail(testSupplier.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenUserIdAlreadyExists_ShouldThrowException() {
        // Given
        testSupplier.setId(null); // Make it a new supplier
        when(supplierRepository.existsByEmail(testSupplier.getEmail())).thenReturn(false);
        when(supplierRepository.existsByUserId(testSupplier.getUserId())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.save(testSupplier));
    }

    @Test
    void save_WhenUpdatingExistingSupplierWithSameEmail_ShouldNotCheckDuplicateEmail() {
        // Given
        testSupplier.setId(1L);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        // When
        Supplier result = supplierService.save(testSupplier);

        // Then
        assertNotNull(result);
        verify(supplierRepository, never()).existsByEmail(any());
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(supplierRepository.existsById(1L)).thenReturn(true);

        // When
        supplierService.deleteById(1L);

        // Then
        verify(supplierRepository).deleteById(1L);
    }

    @Test
    void deleteById_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.deleteById(null));
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(supplierRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.deleteById(1L));
    }

    @Test
    void findByUserId_WhenValidId_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findByUserId(100L)).thenReturn(Optional.of(testSupplier));

        // When
        Optional<Supplier> result = supplierService.findByUserId(100L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier, result.get());
    }

    @Test
    void findByUserId_WhenIdNull_ShouldReturnEmpty() {
        // When
        Optional<Supplier> result = supplierService.findByUserId(null);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, never()).findByUserId(any());
    }

    @Test
    void findByEmail_WhenValidEmail_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findByEmail("test@supplier.com")).thenReturn(Optional.of(testSupplier));

        // When
        Optional<Supplier> result = supplierService.findByEmail("test@supplier.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier, result.get());
    }

    @Test
    void findByEmail_WhenEmailNull_ShouldReturnEmpty() {
        // When
        Optional<Supplier> result = supplierService.findByEmail(null);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, never()).findByEmail(any());
    }

    @Test
    void findByEmail_WhenEmailEmpty_ShouldReturnEmpty() {
        // When
        Optional<Supplier> result = supplierService.findByEmail("");

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, never()).findByEmail(any());
    }

    @Test
    void findVerified_ShouldReturnVerifiedSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByVerifiedTrue()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findVerified();

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findActive_ShouldReturnActiveSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByActiveTrue()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findActive();

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findVerifiedAndActive_ShouldReturnVerifiedAndActiveSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByVerifiedTrueAndActiveTrue()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findVerifiedAndActive();

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findByCity_WhenValidCity_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByCity("Test City")).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findByCity("Test City");

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findByCity_WhenCityNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.findByCity(null));
    }

    @Test
    void findByCity_WhenCityEmpty_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.findByCity(""));
    }

    @Test
    void findByState_WhenValidState_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByState("Test State")).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findByState("Test State");

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findByState_WhenStateNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.findByState(null));
    }

    @Test
    void findByCountry_WhenValidCountry_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByCountry("Test Country")).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findByCountry("Test Country");

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findByCountry_WhenCountryNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.findByCountry(null));
    }

    @Test
    void searchByName_WhenValidName_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByNameContainingIgnoreCase("Test")).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.searchByName("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void searchByName_WhenNameNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.searchByName(null));
    }

    @Test
    void findSuppliersWithinRadius_WhenValidParams_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findSuppliersWithinRadius(40.7128, -74.0060, 10.0)).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.findSuppliersWithinRadius(40.7128, -74.0060, 10.0);

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
    }

    @Test
    void findSuppliersWithinRadius_WhenLatitudeNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(null, -74.0060, 10.0));
    }

    @Test
    void findSuppliersWithinRadius_WhenLongitudeNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(40.7128, null, 10.0));
    }

    @Test
    void findSuppliersWithinRadius_WhenRadiusNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(40.7128, -74.0060, null));
    }

    @Test
    void findSuppliersWithinRadius_WhenRadiusNegative_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(40.7128, -74.0060, -1.0));
    }

    @Test
    void findSuppliersWithinRadius_WhenLatitudeOutOfRange_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(91.0, -74.0060, 10.0));
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(-91.0, -74.0060, 10.0));
    }

    @Test
    void findSuppliersWithinRadius_WhenLongitudeOutOfRange_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(40.7128, 181.0, 10.0));
        assertThrows(IllegalArgumentException.class, 
                () -> supplierService.findSuppliersWithinRadius(40.7128, -181.0, 10.0));
    }

    @Test
    void verifySupplier_WhenValidId_ShouldVerifyAndReturn() {
        // Given
        testSupplier.setVerified(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        // When
        Supplier result = supplierService.verifySupplier(1L);

        // Then
        assertTrue(result.isVerified());
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void verifySupplier_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.verifySupplier(null));
    }

    @Test
    void verifySupplier_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.verifySupplier(1L));
    }

    @Test
    void activateSupplier_WhenValidId_ShouldActivateAndReturn() {
        // Given
        testSupplier.setActive(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        // When
        Supplier result = supplierService.activateSupplier(1L);

        // Then
        assertTrue(result.isActive());
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void activateSupplier_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.activateSupplier(null));
    }

    @Test
    void deactivateSupplier_WhenValidId_ShouldDeactivateAndReturn() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        // When
        Supplier result = supplierService.deactivateSupplier(1L);

        // Then
        assertFalse(result.isActive());
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    void deactivateSupplier_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> supplierService.deactivateSupplier(null));
    }
}