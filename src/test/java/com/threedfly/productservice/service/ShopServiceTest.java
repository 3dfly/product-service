package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.repository.ShopRepository;
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
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;

    private Shop testShop;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId(1L);
        testShop.setSellerId(100L);
        testShop.setName("Test Shop");
        testShop.setDescription("Test shop description");
        testShop.setAddress("123 Test St, Test City");
        testShop.setContactInfo("test@shop.com");
    }

    @Test
    void findAll_ShouldReturnAllShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findAll()).thenReturn(shops);

        // When
        List<Shop> result = shopService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
        verify(shopRepository).findAll();
    }

    @Test
    void findById_WhenIdExists_ShouldReturnShop() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));

        // When
        Optional<Shop> result = shopService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testShop, result.get());
    }

    @Test
    void findById_WhenIdNull_ShouldReturnEmpty() {
        // When
        Optional<Shop> result = shopService.findById(null);

        // Then
        assertFalse(result.isPresent());
        verify(shopRepository, never()).findById(any());
    }

    @Test
    void findById_WhenIdNotExists_ShouldReturnEmpty() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Shop> result = shopService.findById(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_WhenValidShop_ShouldSaveAndReturn() {
        // Given
        when(shopRepository.save(testShop)).thenReturn(testShop);

        // When
        Shop result = shopService.save(testShop);

        // Then
        assertNotNull(result);
        assertEquals(testShop, result);
        verify(shopRepository).save(testShop);
    }

    @Test
    void save_WhenShopNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.save(null));
        verify(shopRepository, never()).save(any());
    }

    @Test
    void save_WhenNameNull_ShouldThrowException() {
        // Given
        testShop.setName(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.save(testShop));
    }

    @Test
    void save_WhenNameEmpty_ShouldThrowException() {
        // Given
        testShop.setName("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.save(testShop));
    }

    @Test
    void save_WhenSellerIdNull_ShouldThrowException() {
        // Given
        testShop.setSellerId(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.save(testShop));
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(shopRepository.existsById(1L)).thenReturn(true);

        // When
        shopService.deleteById(1L);

        // Then
        verify(shopRepository).deleteById(1L);
    }

    @Test
    void deleteById_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.deleteById(null));
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.deleteById(1L));
    }

    @Test
    void findBySellerId_WhenValidId_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findBySellerId(100L)).thenReturn(shops);

        // When
        List<Shop> result = shopService.findBySellerId(100L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
    }

    @Test
    void findBySellerId_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.findBySellerId(null));
    }

    @Test
    void findFirstBySellerId_WhenValidId_ShouldReturnShop() {
        // Given
        when(shopRepository.findFirstBySellerId(100L)).thenReturn(Optional.of(testShop));

        // When
        Optional<Shop> result = shopService.findFirstBySellerId(100L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testShop, result.get());
    }

    @Test
    void findFirstBySellerId_WhenIdNull_ShouldReturnEmpty() {
        // When
        Optional<Shop> result = shopService.findFirstBySellerId(null);

        // Then
        assertFalse(result.isPresent());
        verify(shopRepository, never()).findFirstBySellerId(any());
    }

    @Test
    void searchByName_WhenValidName_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByNameContainingIgnoreCase("Test")).thenReturn(shops);

        // When
        List<Shop> result = shopService.searchByName("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
    }

    @Test
    void searchByName_WhenNameNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByName(null));
    }

    @Test
    void searchByName_WhenNameEmpty_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByName(""));
    }

    @Test
    void searchByDescription_WhenValidKeyword_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByDescriptionContainingIgnoreCase("description")).thenReturn(shops);

        // When
        List<Shop> result = shopService.searchByDescription("description");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
    }

    @Test
    void searchByDescription_WhenKeywordNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByDescription(null));
    }

    @Test
    void searchByDescription_WhenKeywordEmpty_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByDescription(""));
    }

    @Test
    void searchByAddress_WhenValidAddress_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByAddressContainingIgnoreCase("Test")).thenReturn(shops);

        // When
        List<Shop> result = shopService.searchByAddress("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
    }

    @Test
    void searchByAddress_WhenAddressNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByAddress(null));
    }

    @Test
    void searchByAddress_WhenAddressEmpty_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.searchByAddress(""));
    }

    @Test
    void findShopsWithProducts_ShouldReturnShopsWithProducts() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findShopsWithProducts()).thenReturn(shops);

        // When
        List<Shop> result = shopService.findShopsWithProducts();

        // Then
        assertEquals(1, result.size());
        assertEquals(testShop, result.get(0));
    }

    @Test
    void countProductsByShopId_WhenValidId_ShouldReturnCount() {
        // Given
        when(shopRepository.existsById(1L)).thenReturn(true);
        when(shopRepository.countProductsByShopId(1L)).thenReturn(5L);

        // When
        Long result = shopService.countProductsByShopId(1L);

        // Then
        assertEquals(5L, result);
    }

    @Test
    void countProductsByShopId_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> shopService.countProductsByShopId(null));
    }

    @Test
    void countProductsByShopId_WhenShopNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.countProductsByShopId(1L));
    }

    @Test
    void existsBySellerId_WhenValidId_ShouldReturnTrue() {
        // Given
        when(shopRepository.existsBySellerId(100L)).thenReturn(true);

        // When
        boolean result = shopService.existsBySellerId(100L);

        // Then
        assertTrue(result);
    }

    @Test
    void existsBySellerId_WhenIdNull_ShouldReturnFalse() {
        // When
        boolean result = shopService.existsBySellerId(null);

        // Then
        assertFalse(result);
        verify(shopRepository, never()).existsBySellerId(any());
    }

    @Test
    void updateShopInfo_WhenValidParams_ShouldUpdateAndReturn() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        // When
        Shop result = shopService.updateShopInfo(1L, "New Name", "New Description", "New Address", "New Contact");

        // Then
        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals("New Address", result.getAddress());
        assertEquals("New Contact", result.getContactInfo());
        verify(shopRepository).save(testShop);
    }

    @Test
    void updateShopInfo_WhenIdNull_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> shopService.updateShopInfo(null, "Name", "Desc", "Addr", "Contact"));
    }

    @Test
    void updateShopInfo_WhenShopNotFound_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> shopService.updateShopInfo(1L, "Name", "Desc", "Addr", "Contact"));
    }

    @Test
    void updateShopInfo_WhenNameEmpty_ShouldNotUpdateName() {
        // Given
        String originalName = testShop.getName();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        // When
        Shop result = shopService.updateShopInfo(1L, "", null, null, null);

        // Then
        assertEquals(originalName, result.getName()); // Name should remain unchanged
    }

    @Test
    void updateShopInfo_WhenOnlyNameProvided_ShouldUpdateOnlyName() {
        // Given
        String originalDescription = testShop.getDescription();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);

        // When
        Shop result = shopService.updateShopInfo(1L, "New Name", null, null, null);

        // Then
        assertEquals("New Name", result.getName());
        assertEquals(originalDescription, result.getDescription()); // Should remain unchanged
    }
}