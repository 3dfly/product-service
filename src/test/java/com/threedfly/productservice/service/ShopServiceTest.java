package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ShopRequest;
import com.threedfly.productservice.dto.ShopResponse;
import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.mapper.ShopMapper;
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

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private ShopService shopService;

    private Shop testShop;
    private ShopRequest testShopRequest;
    private ShopResponse testShopResponse;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId(1L);
        testShop.setSellerId(100L);
        testShop.setName("Test Shop");
        testShop.setDescription("Test shop description");
        testShop.setAddress("123 Test St, Test City");
        testShop.setContactInfo("test@shop.com");

        testShopRequest = new ShopRequest();
        testShopRequest.setSellerId(100L);
        testShopRequest.setName("Test Shop");
        testShopRequest.setDescription("Test shop description");
        testShopRequest.setAddress("123 Test St, Test City");
        testShopRequest.setContactInfo("test@shop.com");

        testShopResponse = new ShopResponse();
        testShopResponse.setId(1L);
        testShopResponse.setSellerId(100L);
        testShopResponse.setName("Test Shop");
        testShopResponse.setDescription("Test shop description");
        testShopResponse.setAddress("123 Test St, Test City");
        testShopResponse.setContactInfo("test@shop.com");
        testShopResponse.setProductCount(0L);
    }

    @Test
    void findAll_ShouldReturnAllShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        List<ShopResponse> expectedResponses = Arrays.asList(testShopResponse);
        when(shopRepository.findAll()).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        assertEquals(testShopResponse.getSellerId(), result.get(0).getSellerId());
        verify(shopRepository).findAll();
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void findById_WhenIdExists_ShouldReturnShop() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testShopResponse.getName(), result.getName());
        assertEquals(testShopResponse.getSellerId(), result.getSellerId());
        verify(shopRepository).findById(1L);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void findById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.findById(1L));
        verify(shopRepository).findById(1L);
        verify(shopMapper, never()).toResponse(any());
    }

    @Test
    void save_WhenValidShop_ShouldSaveAndReturn() {
        // Given
        when(shopMapper.toEntity(testShopRequest)).thenReturn(testShop);
        when(shopRepository.save(testShop)).thenReturn(testShop);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.save(testShopRequest);

        // Then
        assertNotNull(result);
        assertEquals(testShopResponse.getName(), result.getName());
        assertEquals(testShopResponse.getSellerId(), result.getSellerId());
        verify(shopMapper).toEntity(testShopRequest);
        verify(shopRepository).save(testShop);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void deleteById_WhenIdExists_ShouldDelete() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));

        // When
        shopService.deleteById(1L);

        // Then
        verify(shopRepository).findById(1L);
        verify(shopRepository).delete(testShop);
    }

    @Test
    void deleteById_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.deleteById(1L));
        verify(shopRepository).findById(1L);
        verify(shopRepository, never()).delete(any());
    }

    @Test
    void findBySellerId_WhenValidId_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findBySellerId(100L)).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.findBySellerId(100L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        verify(shopRepository).findBySellerId(100L);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void findFirstBySellerId_WhenValidId_ShouldReturnShop() {
        // Given
        when(shopRepository.findFirstBySellerId(100L)).thenReturn(Optional.of(testShop));
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.findFirstBySellerId(100L);

        // Then
        assertNotNull(result);
        assertEquals(testShopResponse.getName(), result.getName());
        verify(shopRepository).findFirstBySellerId(100L);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void findFirstBySellerId_WhenIdNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.findFirstBySellerId(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.findFirstBySellerId(100L));
        verify(shopRepository).findFirstBySellerId(100L);
        verify(shopMapper, never()).toResponse(any());
    }

    @Test
    void searchByName_WhenValidName_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByNameContainingIgnoreCase("Test")).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.searchByName("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        verify(shopRepository).findByNameContainingIgnoreCase("Test");
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void searchByDescription_WhenValidKeyword_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByDescriptionContainingIgnoreCase("description")).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.searchByDescription("description");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        verify(shopRepository).findByDescriptionContainingIgnoreCase("description");
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void searchByAddress_WhenValidAddress_ShouldReturnShops() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findByAddressContainingIgnoreCase("Test")).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.searchByAddress("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        verify(shopRepository).findByAddressContainingIgnoreCase("Test");
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void findShopsWithProducts_ShouldReturnShopsWithProducts() {
        // Given
        List<Shop> shops = Arrays.asList(testShop);
        when(shopRepository.findShopsWithProducts()).thenReturn(shops);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        List<ShopResponse> result = shopService.findShopsWithProducts();

        // Then
        assertEquals(1, result.size());
        assertEquals(testShopResponse.getName(), result.get(0).getName());
        verify(shopRepository).findShopsWithProducts();
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void update_WhenValidParams_ShouldUpdateAndReturn() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.update(1L, testShopRequest);

        // Then
        assertNotNull(result);
        assertEquals(testShopResponse.getName(), result.getName());
        verify(shopRepository).findById(1L);
        verify(shopMapper).updateEntityFromRequest(testShop, testShopRequest);
        verify(shopRepository).save(testShop);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void update_WhenShopNotFound_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.update(1L, testShopRequest));
        verify(shopRepository).findById(1L);
        verify(shopMapper, never()).updateEntityFromRequest(any(), any());
        verify(shopRepository, never()).save(any());
    }

    @Test
    void updateShopInfo_WhenValidParams_ShouldUpdateAndReturn() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.updateShopInfo(1L, "New Name", "New Description", "New Address", "New Contact");

        // Then
        assertNotNull(result);
        verify(shopRepository).findById(1L);
        verify(shopRepository).save(testShop);
        verify(shopMapper).toResponse(testShop);
        assertEquals("New Name", testShop.getName());
        assertEquals("New Description", testShop.getDescription());
        assertEquals("New Address", testShop.getAddress());
        assertEquals("New Contact", testShop.getContactInfo());
    }

    @Test
    void updateShopInfo_WhenShopNotFound_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> shopService.updateShopInfo(1L, "Name", "Desc", "Addr", "Contact"));
        verify(shopRepository).findById(1L);
        verify(shopRepository, never()).save(any());
    }

    @Test
    void updateShopInfo_WhenNameEmpty_ShouldNotUpdateName() {
        // Given
        String originalName = testShop.getName();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.updateShopInfo(1L, "", null, null, null);

        // Then
        assertEquals(originalName, testShop.getName()); // Name should remain unchanged
        verify(shopRepository).save(testShop);
        verify(shopMapper).toResponse(testShop);
    }

    @Test
    void updateShopInfo_WhenOnlyNameProvided_ShouldUpdateOnlyName() {
        // Given
        String originalDescription = testShop.getDescription();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(testShop);
        when(shopMapper.toResponse(testShop)).thenReturn(testShopResponse);

        // When
        ShopResponse result = shopService.updateShopInfo(1L, "New Name", null, null, null);

        // Then
        assertEquals("New Name", testShop.getName());
        assertEquals(originalDescription, testShop.getDescription()); // Should remain unchanged
        verify(shopRepository).save(testShop);
        verify(shopMapper).toResponse(testShop);
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
        verify(shopRepository).existsById(1L);
        verify(shopRepository).countProductsByShopId(1L);
    }

    @Test
    void countProductsByShopId_WhenShopNotExists_ShouldThrowException() {
        // Given
        when(shopRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> shopService.countProductsByShopId(1L));
        verify(shopRepository).existsById(1L);
        verify(shopRepository, never()).countProductsByShopId(any());
    }

    @Test
    void existsBySellerId_WhenValidId_ShouldReturnTrue() {
        // Given
        when(shopRepository.existsBySellerId(100L)).thenReturn(true);

        // When
        boolean result = shopService.existsBySellerId(100L);

        // Then
        assertTrue(result);
        verify(shopRepository).existsBySellerId(100L);
    }

    @Test
    void existsBySellerId_WhenValidId_ShouldReturnFalse() {
        // Given
        when(shopRepository.existsBySellerId(100L)).thenReturn(false);

        // When
        boolean result = shopService.existsBySellerId(100L);

        // Then
        assertFalse(result);
        verify(shopRepository).existsBySellerId(100L);
    }
}