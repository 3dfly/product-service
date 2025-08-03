package com.threedfly.productservice.controller;

import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.service.FilamentStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filament-stock")
public class FilamentStockController {
    
    private final FilamentStockService filamentStockService;
    
    @GetMapping
    public ResponseEntity<List<FilamentStock>> getAllFilamentStock() {
        List<FilamentStock> stocks = filamentStockService.findAll();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FilamentStock> getFilamentStockById(@PathVariable Long id) {
        return filamentStockService.findById(id)
                .map(stock -> ResponseEntity.ok(stock))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<FilamentStock> createFilamentStock(@RequestBody FilamentStock filamentStock) {
        FilamentStock saved = filamentStockService.save(filamentStock);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FilamentStock> updateFilamentStock(@PathVariable Long id, 
                                                           @RequestBody FilamentStock filamentStock) {
        filamentStock.setId(id);
        FilamentStock updated = filamentStockService.save(filamentStock);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilamentStock(@PathVariable Long id) {
        filamentStockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<FilamentStock>> getFilamentStockBySupplierId(@PathVariable Long supplierId) {
        List<FilamentStock> stocks = filamentStockService.findBySupplierId(supplierId);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/material-type/{materialType}")
    public ResponseEntity<List<FilamentStock>> getFilamentStockByMaterialType(@PathVariable FilamentType materialType) {
        List<FilamentStock> stocks = filamentStockService.findByMaterialType(materialType);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/color/{color}")
    public ResponseEntity<List<FilamentStock>> getFilamentStockByColor(@PathVariable String color) {
        List<FilamentStock> stocks = filamentStockService.findByColor(color);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<FilamentStock>> getAvailableFilamentStock() {
        List<FilamentStock> stocks = filamentStockService.findAvailable();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FilamentStock>> searchFilamentStock(
            @RequestParam FilamentType materialType,
            @RequestParam String color) {
        List<FilamentStock> stocks = filamentStockService.findByMaterialTypeAndColor(materialType, color);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/sufficient-quantity")
    public ResponseEntity<List<FilamentStock>> getStockWithSufficientQuantity(@RequestParam Double requiredKg) {
        List<FilamentStock> stocks = filamentStockService.findStockWithSufficientQuantity(requiredKg);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<FilamentStock>> getLowStockItems(@RequestParam Double threshold) {
        List<FilamentStock> stocks = filamentStockService.findLowStockItems(threshold);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<FilamentStock>> getExpiredStock() {
        List<FilamentStock> stocks = filamentStockService.findExpiredStock();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/count/material-type/{materialType}")
    public ResponseEntity<Long> countAvailableByMaterialType(@PathVariable FilamentType materialType) {
        Long count = filamentStockService.countAvailableByMaterialType(materialType);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/{id}/reserve")
    public ResponseEntity<FilamentStock> reserveStock(@PathVariable Long id, @RequestParam Double quantityKg) {
        FilamentStock stock = filamentStockService.reserveStock(id, quantityKg);
        return ResponseEntity.ok(stock);
    }
    
    @PostMapping("/{id}/release")
    public ResponseEntity<FilamentStock> releaseReservedStock(@PathVariable Long id, @RequestParam Double quantityKg) {
        FilamentStock stock = filamentStockService.releaseReservedStock(id, quantityKg);
        return ResponseEntity.ok(stock);
    }
}