package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.SupplierRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.SupplierMapper;
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

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;
    private SupplierRequest testSupplierRequest;
    private SupplierResponse testSupplierResponse;

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

        testSupplierRequest = SupplierRequest.builder()
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

        testSupplierResponse = SupplierResponse.builder()
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
                .stockCount(0)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testSupplierResponse.getName(), result.get(0).getName());
        assertEquals(testSupplierResponse.getEmail(), result.get(0).getEmail());
        verify(supplierRepository).findAll();
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findById_WhenIdExists_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testSupplierResponse.getName(), result.getName());
        assertEquals(testSupplierResponse.getEmail(), result.getEmail());
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.findById(1L));
        verify(supplierRepository).findById(1L);
        verify(supplierMapper, never()).toResponse(any());
    }

    @Test
    void save_WhenValidSupplier_ShouldSaveAndReturn() {
        // Given
        when(supplierRepository.existsByEmail(testSupplierRequest.getEmail())).thenReturn(false);
        when(supplierRepository.existsByUserId(testSupplierRequest.getUserId())).thenReturn(false);
        when(supplierMapper.toEntity(testSupplierRequest)).thenReturn(testSupplier);
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.save(testSupplierRequest);

        // Then
        assertNotNull(result);
        assertEquals(testSupplierResponse.getName(), result.getName());
        assertEquals(testSupplierResponse.getEmail(), result.getEmail());
        verify(supplierRepository).existsByEmail(testSupplierRequest.getEmail());
        verify(supplierMapper).toEntity(testSupplierRequest);
        verify(supplierRepository).save(testSupplier);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void save_WhenEmailAlreadyExists_ShouldThrowException() {
        // Given
        when(supplierRepository.existsByEmail(testSupplierRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.save(testSupplierRequest));
        verify(supplierRepository).existsByEmail(testSupplierRequest.getEmail());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void save_WhenUserIdAlreadyExists_ShouldThrowException() {
        // Given
        when(supplierRepository.existsByEmail(testSupplierRequest.getEmail())).thenReturn(false);
        when(supplierRepository.existsByUserId(testSupplierRequest.getUserId())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.save(testSupplierRequest));
        verify(supplierRepository).existsByEmail(testSupplierRequest.getEmail());
        verify(supplierRepository).existsByUserId(testSupplierRequest.getUserId());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When
        supplierService.deleteById(1L);

        // Then
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).delete(testSupplier);
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.deleteById(1L));
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).delete(any());
    }

    @Test
    void findByUserId_WhenUserIdExists_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findByUserId(100L)).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.findByUserId(100L);

        // Then
        assertNotNull(result);
        assertEquals(testSupplierResponse.getUserId(), result.getUserId());
        verify(supplierRepository).findByUserId(100L);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findByUserId_WhenUserIdNotExists_ShouldThrowException() {
        // Given
        when(supplierRepository.findByUserId(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.findByUserId(100L));
        verify(supplierRepository).findByUserId(100L);
        verify(supplierMapper, never()).toResponse(any());
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findByEmail("test@supplier.com")).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.findByEmail("test@supplier.com");

        // Then
        assertNotNull(result);
        assertEquals(testSupplierResponse.getEmail(), result.getEmail());
        verify(supplierRepository).findByEmail("test@supplier.com");
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findByEmail_WhenEmailNotExists_ShouldThrowException() {
        // Given
        when(supplierRepository.findByEmail("test@supplier.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.findByEmail("test@supplier.com"));
        verify(supplierRepository).findByEmail("test@supplier.com");
        verify(supplierMapper, never()).toResponse(any());
    }

    @Test
    void findVerified_ShouldReturnVerifiedSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByVerifiedTrue()).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findVerified();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isVerified());
        verify(supplierRepository).findByVerifiedTrue();
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findActive_ShouldReturnActiveSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByActiveTrue()).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findActive();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(supplierRepository).findByActiveTrue();
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findVerifiedAndActive_ShouldReturnVerifiedAndActiveSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByVerifiedTrueAndActiveTrue()).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findVerifiedAndActive();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isVerified());
        assertTrue(result.get(0).isActive());
        verify(supplierRepository).findByVerifiedTrueAndActiveTrue();
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findByCity_WhenValidCity_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByCity("Test City")).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findByCity("Test City");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test City", result.get(0).getCity());
        verify(supplierRepository).findByCity("Test City");
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findByState_WhenValidState_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByState("Test State")).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findByState("Test State");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test State", result.get(0).getState());
        verify(supplierRepository).findByState("Test State");
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findByCountry_WhenValidCountry_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByCountry("Test Country")).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findByCountry("Test Country");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Country", result.get(0).getCountry());
        verify(supplierRepository).findByCountry("Test Country");
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void searchByName_WhenValidName_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByNameContainingIgnoreCase("Test")).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.searchByName("Test");

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Test"));
        verify(supplierRepository).findByNameContainingIgnoreCase("Test");
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void findSuppliersWithinRadius_WhenValidParams_ShouldReturnSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findSuppliersWithinRadius(40.7128, -74.0060, 10.0)).thenReturn(suppliers);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        List<SupplierResponse> result = supplierService.findSuppliersWithinRadius(40.7128, -74.0060, 10.0);

        // Then
        assertEquals(1, result.size());
        assertEquals(40.7128, result.get(0).getLatitude());
        assertEquals(-74.0060, result.get(0).getLongitude());
        verify(supplierRepository).findSuppliersWithinRadius(40.7128, -74.0060, 10.0);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void verifySupplier_WhenSupplierExists_ShouldVerifyAndReturn() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.verifySupplier(1L);

        // Then
        assertNotNull(result);
        assertTrue(testSupplier.isVerified());
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).save(testSupplier);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void verifySupplier_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.verifySupplier(1L));
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void activateSupplier_WhenSupplierExists_ShouldActivateAndReturn() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.activateSupplier(1L);

        // Then
        assertNotNull(result);
        assertTrue(testSupplier.isActive());
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).save(testSupplier);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void activateSupplier_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.activateSupplier(1L));
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void deactivateSupplier_WhenSupplierExists_ShouldDeactivateAndReturn() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);
        when(supplierMapper.toResponse(testSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.deactivateSupplier(1L);

        // Then
        assertNotNull(result);
        assertFalse(testSupplier.isActive());
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).save(testSupplier);
        verify(supplierMapper).toResponse(testSupplier);
    }

    @Test
    void deactivateSupplier_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.deactivateSupplier(1L));
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void update_WhenValidParams_ShouldUpdateAndReturn() {
        // Given
        // Create existing supplier with the same email so no duplicate check is triggered
        Supplier existingSupplier = Supplier.builder()
                .email(testSupplierRequest.getEmail())
                .userId(testSupplierRequest.getUserId())
                .build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(existingSupplier)).thenReturn(existingSupplier);
        when(supplierMapper.toResponse(existingSupplier)).thenReturn(testSupplierResponse);

        // When
        SupplierResponse result = supplierService.update(1L, testSupplierRequest);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).updateEntityFromRequest(existingSupplier, testSupplierRequest);
        verify(supplierRepository).save(existingSupplier);
        verify(supplierMapper).toResponse(existingSupplier);
    }

    @Test
    void update_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.update(1L, testSupplierRequest));
        verify(supplierRepository).findById(1L);
        verify(supplierMapper, never()).updateEntityFromRequest(any(), any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void update_WhenEmailAlreadyExists_ShouldThrowException() {
        // Given
        Supplier existingSupplier = Supplier.builder()
                .email("different@email.com")
                .build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.existsByEmail(testSupplierRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> supplierService.update(1L, testSupplierRequest));
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).existsByEmail(testSupplierRequest.getEmail());
        verify(supplierRepository, never()).save(any());
    }
}