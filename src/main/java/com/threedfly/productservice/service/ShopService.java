package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShopService {
    
    private final ShopRepository shopRepository;
    
    public List<Shop> findAll() {
        log.info("Finding all shops");
        return shopRepository.findAll();
    }
    
    public Optional<Shop> findById(Long id) {
        log.info("Finding shop by id: {}", id);

        if (id == null) {
            return Optional.empty();
        }
        return shopRepository.findById(id);
    }
    
    public Shop save(Shop shop) {
        log.info("Saving shop: {}", shop);
        if (shop == null) {
            throw new IllegalArgumentException("Shop cannot be null");
        }
        
        // Validate required fields
        if (shop.getName() == null || shop.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Shop name is required");
        }
        
        if (shop.getSellerId() == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        
        return shopRepository.save(shop);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting shop by id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        if (!shopRepository.existsById(id)) {
            throw new RuntimeException("Shop not found with ID: " + id);
        }
        
        shopRepository.deleteById(id);
    }
    
    public List<Shop> findBySellerId(Long sellerId) {
        log.info("Finding shops by seller id: {}", sellerId);
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }
        return shopRepository.findBySellerId(sellerId);
    }
    
    public Optional<Shop> findFirstBySellerId(Long sellerId) {
        log.info("Finding first shop by seller id: {}", sellerId);
        if (sellerId == null) {
            return Optional.empty();
        }
        return shopRepository.findFirstBySellerId(sellerId);
    }
    
    public List<Shop> searchByName(String name) {
        log.info("Searching shops by name: {}", name);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return shopRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Shop> searchByDescription(String keyword) {
        log.info("Searching shops by description keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        return shopRepository.findByDescriptionContainingIgnoreCase(keyword);
    }
    
    public List<Shop> searchByAddress(String address) {
        log.info("Searching shops by address: {}", address);
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        return shopRepository.findByAddressContainingIgnoreCase(address);
    }
    
    public List<Shop> findShopsWithProducts() {
        log.info("Finding shops with products");
        return shopRepository.findShopsWithProducts();
    }
    
    public Long countProductsByShopId(Long shopId) {
        log.info("Counting products for shop id: {}", shopId);
        if (shopId == null) {
            throw new IllegalArgumentException("Shop ID cannot be null");
        }
        
        // Verify shop exists
        if (!shopRepository.existsById(shopId)) {
            throw new RuntimeException("Shop not found with ID: " + shopId);
        }
        
        return shopRepository.countProductsByShopId(shopId);
    }
    
    public boolean existsBySellerId(Long sellerId) {
        log.info("Checking if shop exists for seller id: {}", sellerId);
        if (sellerId == null) {
            return false;
        }
        return shopRepository.existsBySellerId(sellerId);
    }
    
    public Shop updateShopInfo(Long id, String name, String description, String address, String contactInfo) {
        log.info("Updating shop info for id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + id));
        
        if (name != null && !name.trim().isEmpty()) {
            shop.setName(name);
        }
        
        if (description != null) {
            shop.setDescription(description);
        }
        
        if (address != null) {
            shop.setAddress(address);
        }
        
        if (contactInfo != null) {
            shop.setContactInfo(contactInfo);
        }
        
        return shopRepository.save(shop);
    }
}