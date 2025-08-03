package com.threedfly.productservice.controller;

import com.threedfly.productservice.entity.Shop;
import com.threedfly.productservice.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {
    
    private final ShopService shopService;
    
    @GetMapping
    public ResponseEntity<List<Shop>> getAllShops() {
        List<Shop> shops = shopService.findAll();
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShopById(@PathVariable Long id) {
        return shopService.findById(id)
                .map(shop -> ResponseEntity.ok(shop))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Shop> createShop(@RequestBody Shop shop) {
        Shop saved = shopService.save(shop);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Shop> updateShop(@PathVariable Long id, @RequestBody Shop shop) {
        shop.setId(id);
        Shop updated = shopService.save(shop);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Shop>> getShopsBySellerId(@PathVariable Long sellerId) {
        List<Shop> shops = shopService.findBySellerId(sellerId);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/seller/{sellerId}/first")
    public ResponseEntity<Shop> getFirstShopBySellerId(@PathVariable Long sellerId) {
        return shopService.findFirstBySellerId(sellerId)
                .map(shop -> ResponseEntity.ok(shop))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<List<Shop>> searchShopsByName(@RequestParam String name) {
        List<Shop> shops = shopService.searchByName(name);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/search/description")
    public ResponseEntity<List<Shop>> searchShopsByDescription(@RequestParam String keyword) {
        List<Shop> shops = shopService.searchByDescription(keyword);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/search/address")
    public ResponseEntity<List<Shop>> searchShopsByAddress(@RequestParam String address) {
        List<Shop> shops = shopService.searchByAddress(address);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/with-products")
    public ResponseEntity<List<Shop>> getShopsWithProducts() {
        List<Shop> shops = shopService.findShopsWithProducts();
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/{id}/products/count")
    public ResponseEntity<Long> countProductsByShopId(@PathVariable Long id) {
        Long count = shopService.countProductsByShopId(id);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/exists/seller/{sellerId}")
    public ResponseEntity<Boolean> checkIfShopExistsForSeller(@PathVariable Long sellerId) {
        boolean exists = shopService.existsBySellerId(sellerId);
        return ResponseEntity.ok(exists);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Shop> updateShopInfo(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contactInfo) {
        Shop updated = shopService.updateShopInfo(id, name, description, address, contactInfo);
        return ResponseEntity.ok(updated);
    }
}