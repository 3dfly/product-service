package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ShopRequest;
import com.threedfly.productservice.dto.ShopResponse;
import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.mapper.ShopMapper;
import com.threedfly.productservice.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShopService {
    
    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    
    public List<ShopResponse> findAll() {
        log.info("Finding all shops");
        return shopRepository.findAll()
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public ShopResponse findById(Long id) {
        log.info("Finding shop by id: {}", id);
        
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + id));
        
        return shopMapper.toResponse(shop);
    }
    
    public ShopResponse save(ShopRequest request) {
        log.info("Saving shop: {}", request);
        
        Shop shop = shopMapper.toEntity(request);
        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toResponse(savedShop);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting shop by id: {}", id);
        
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + id));
        
        shopRepository.delete(shop);
    }
    
    public List<ShopResponse> findBySellerId(Long sellerId) {
        log.info("Finding shops by seller id: {}", sellerId);
        return shopRepository.findBySellerId(sellerId)
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public ShopResponse findFirstBySellerId(Long sellerId) {
        log.info("Finding first shop by seller id: {}", sellerId);
        
        Shop shop = shopRepository.findFirstBySellerId(sellerId)
                .orElseThrow(() -> new RuntimeException("Shop not found for seller ID: " + sellerId));
        
        return shopMapper.toResponse(shop);
    }
    
    public List<ShopResponse> searchByName(String name) {
        log.info("Searching shops by name: {}", name);
        return shopRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShopResponse> searchByDescription(String keyword) {
        log.info("Searching shops by description keyword: {}", keyword);
        return shopRepository.findByDescriptionContainingIgnoreCase(keyword)
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShopResponse> searchByAddress(String address) {
        log.info("Searching shops by address: {}", address);
        return shopRepository.findByAddressContainingIgnoreCase(address)
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShopResponse> findShopsWithProducts() {
        log.info("Finding shops with products");
        return shopRepository.findShopsWithProducts()
                .stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public Long countProductsByShopId(Long shopId) {
        log.info("Counting products for shop id: {}", shopId);
        
        // Verify shop exists
        if (!shopRepository.existsById(shopId)) {
            throw new RuntimeException("Shop not found with ID: " + shopId);
        }
        
        return shopRepository.countProductsByShopId(shopId);
    }
    
    public boolean existsBySellerId(Long sellerId) {
        log.info("Checking if shop exists for seller id: {}", sellerId);
        return shopRepository.existsBySellerId(sellerId);
    }
    
    public ShopResponse update(Long id, ShopRequest request) {
        log.info("Updating shop with id: {}", id);
        
        Shop existingShop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + id));
        
        shopMapper.updateEntityFromRequest(existingShop, request);
        Shop savedShop = shopRepository.save(existingShop);
        return shopMapper.toResponse(savedShop);
    }
    
    public ShopResponse updateShopInfo(Long id, String name, String description, String address, String contactInfo) {
        log.info("Updating shop info for id: {}", id);
        
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
        
        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toResponse(savedShop);
    }
}