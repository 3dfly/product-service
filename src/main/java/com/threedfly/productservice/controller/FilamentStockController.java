package com.threedfly.productservice.controller;

import com.threedfly.productservice.dto.FilamentStockRequest;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.service.FilamentStockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filament-stock")
@Validated
public class FilamentStockController {
    
    private final FilamentStockService filamentStockService;
    
    @GetMapping
    public ResponseEntity<List<FilamentStockResponse>> getAllFilamentStock() {
        List<FilamentStockResponse> stocks = filamentStockService.findAll();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FilamentStockResponse> getFilamentStockById(@PathVariable @NotNull Long id) {
        FilamentStockResponse stock = filamentStockService.findById(id);
        return ResponseEntity.ok(stock);
    }
    
    @PostMapping
    public ResponseEntity<FilamentStockResponse> createFilamentStock(@Valid @RequestBody FilamentStockRequest request) {
        FilamentStockResponse saved = filamentStockService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FilamentStockResponse> updateFilamentStock(@PathVariable @NotNull Long id, 
                                                           @Valid @RequestBody FilamentStockRequest request) {
        FilamentStockResponse updated = filamentStockService.update(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilamentStock(@PathVariable @NotNull Long id) {
        filamentStockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<FilamentStockResponse>> getFilamentStockBySupplierId(@PathVariable @NotNull Long supplierId) {
        List<FilamentStockResponse> stocks = filamentStockService.findBySupplierId(supplierId);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/material-type/{materialType}")
    public ResponseEntity<List<FilamentStockResponse>> getFilamentStockByMaterialType(@PathVariable @NotNull FilamentType materialType) {
        List<FilamentStockResponse> stocks = filamentStockService.findByMaterialType(materialType);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/color/{color}")
    public ResponseEntity<List<FilamentStockResponse>> getFilamentStockByColor(@PathVariable @NotBlank String color) {
        List<FilamentStockResponse> stocks = filamentStockService.findByColor(color);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<FilamentStockResponse>> getAvailableFilamentStock() {
        List<FilamentStockResponse> stocks = filamentStockService.findAvailable();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FilamentStockResponse>> searchFilamentStock(
            @RequestParam @NotNull FilamentType materialType,
            @RequestParam @NotBlank String color) {
        List<FilamentStockResponse> stocks = filamentStockService.findByMaterialTypeAndColor(materialType, color);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/sufficient-quantity")
    public ResponseEntity<List<FilamentStockResponse>> getStockWithSufficientQuantity(@RequestParam @NotNull @Positive Double requiredKg) {
        List<FilamentStockResponse> stocks = filamentStockService.findStockWithSufficientQuantity(requiredKg);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<FilamentStockResponse>> getLowStockItems(@RequestParam @NotNull @Positive Double threshold) {
        List<FilamentStockResponse> stocks = filamentStockService.findLowStockItems(threshold);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<FilamentStockResponse>> getExpiredStock() {
        List<FilamentStockResponse> stocks = filamentStockService.findExpiredStock();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/count/material-type/{materialType}")
    public ResponseEntity<Long> countAvailableByMaterialType(@PathVariable FilamentType materialType) {
        Long count = filamentStockService.countAvailableByMaterialType(materialType);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/{id}/reserve")
    public ResponseEntity<FilamentStockResponse> reserveStock(@PathVariable @NotNull Long id, @RequestParam @NotNull @Positive Double quantityKg) {
        FilamentStockResponse stock = filamentStockService.reserveStock(id, quantityKg);
        return ResponseEntity.ok(stock);
    }
    
    @PostMapping("/{id}/release")
    public ResponseEntity<FilamentStockResponse> releaseReservedStock(@PathVariable @NotNull Long id, @RequestParam @NotNull @Positive Double quantityKg) {
        FilamentStockResponse stock = filamentStockService.releaseReservedStock(id, quantityKg);
        return ResponseEntity.ok(stock);
    }
}