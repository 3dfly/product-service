package com.threedfly.productservice.controller;

import dto.SupplierRequest;
import dto.SupplierResponse;
import service.SupplierService;
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
@RequestMapping("/suppliers")
@Validated
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable @NotNull Long id) {
        SupplierResponse supplier = supplierService.findById(id);
        return ResponseEntity.ok(supplier);
    }
    
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse saved = supplierService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable @NotNull Long id, @Valid @RequestBody SupplierRequest request) {
        SupplierResponse updated = supplierService.update(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable @NotNull Long id) {
        supplierService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<SupplierResponse> getSupplierByUserId(@PathVariable @NotNull Long userId) {
        SupplierResponse supplier = supplierService.findByUserId(userId);
        return ResponseEntity.ok(supplier);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<SupplierResponse> getSupplierByEmail(@PathVariable @NotBlank String email) {
        SupplierResponse supplier = supplierService.findByEmail(email);
        return ResponseEntity.ok(supplier);
    }
    
    @GetMapping("/verified")
    public ResponseEntity<List<SupplierResponse>> getVerifiedSuppliers() {
        List<SupplierResponse> suppliers = supplierService.findVerified();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        List<SupplierResponse> suppliers = supplierService.findActive();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/verified-and-active")
    public ResponseEntity<List<SupplierResponse>> getVerifiedAndActiveSuppliers() {
        List<SupplierResponse> suppliers = supplierService.findVerifiedAndActive();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByCity(@PathVariable @NotBlank String city) {
        List<SupplierResponse> suppliers = supplierService.findByCity(city);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/state/{state}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByState(@PathVariable @NotBlank String state) {
        List<SupplierResponse> suppliers = supplierService.findByState(state);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByCountry(@PathVariable @NotBlank String country) {
        List<SupplierResponse> suppliers = supplierService.findByCountry(country);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliersByName(@RequestParam @NotBlank String name) {
        List<SupplierResponse> suppliers = supplierService.searchByName(name);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<SupplierResponse>> getSuppliersWithinRadius(
            @RequestParam @NotNull Double latitude,
            @RequestParam @NotNull Double longitude,
            @RequestParam @NotNull @Positive Double radiusKm) {
        List<SupplierResponse> suppliers = supplierService.findSuppliersWithinRadius(latitude, longitude, radiusKm);
        return ResponseEntity.ok(suppliers);
    }
    
    @PostMapping("/{id}/verify")
    public ResponseEntity<SupplierResponse> verifySupplier(@PathVariable @NotNull Long id) {
        SupplierResponse supplier = supplierService.verifySupplier(id);
        return ResponseEntity.ok(supplier);
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<SupplierResponse> activateSupplier(@PathVariable @NotNull Long id) {
        SupplierResponse supplier = supplierService.activateSupplier(id);
        return ResponseEntity.ok(supplier);
    }
    
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<SupplierResponse> deactivateSupplier(@PathVariable @NotNull Long id) {
        SupplierResponse supplier = supplierService.deactivateSupplier(id);
        return ResponseEntity.ok(supplier);
    }
}