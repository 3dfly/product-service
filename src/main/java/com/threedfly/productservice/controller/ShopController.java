package com.threedfly.productservice.controller;

import dto.ShopRequest;
import dto.ShopResponse;
import service.ShopService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
@Validated
public class ShopController {
    
    private final ShopService shopService;
    
    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        List<ShopResponse> shops = shopService.findAll();
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable @NotNull Long id) {
        ShopResponse shop = shopService.findById(id);
        return ResponseEntity.ok(shop);
    }
    
    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody ShopRequest request) {
        ShopResponse saved = shopService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> updateShop(@PathVariable @NotNull Long id, @Valid @RequestBody ShopRequest request) {
        ShopResponse updated = shopService.update(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable @NotNull Long id) {
        shopService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ShopResponse>> getShopsBySellerId(@PathVariable @NotNull Long sellerId) {
        List<ShopResponse> shops = shopService.findBySellerId(sellerId);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/seller/{sellerId}/first")
    public ResponseEntity<ShopResponse> getFirstShopBySellerId(@PathVariable @NotNull Long sellerId) {
        ShopResponse shop = shopService.findFirstBySellerId(sellerId);
        return ResponseEntity.ok(shop);
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<List<ShopResponse>> searchShopsByName(@RequestParam @NotBlank String name) {
        List<ShopResponse> shops = shopService.searchByName(name);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/search/description")
    public ResponseEntity<List<ShopResponse>> searchShopsByDescription(@RequestParam @NotBlank String keyword) {
        List<ShopResponse> shops = shopService.searchByDescription(keyword);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/search/address")
    public ResponseEntity<List<ShopResponse>> searchShopsByAddress(@RequestParam @NotBlank String address) {
        List<ShopResponse> shops = shopService.searchByAddress(address);
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/with-products")
    public ResponseEntity<List<ShopResponse>> getShopsWithProducts() {
        List<ShopResponse> shops = shopService.findShopsWithProducts();
        return ResponseEntity.ok(shops);
    }
    
    @GetMapping("/{id}/products/count")
    public ResponseEntity<Long> countProductsByShopId(@PathVariable @NotNull Long id) {
        Long count = shopService.countProductsByShopId(id);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/exists/seller/{sellerId}")
    public ResponseEntity<Boolean> checkIfShopExistsForSeller(@PathVariable @NotNull Long sellerId) {
        boolean exists = shopService.existsBySellerId(sellerId);
        return ResponseEntity.ok(exists);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ShopResponse> updateShopInfo(
            @PathVariable @NotNull Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contactInfo) {
        ShopResponse updated = shopService.updateShopInfo(id, name, description, address, contactInfo);
        return ResponseEntity.ok(updated);
    }
}