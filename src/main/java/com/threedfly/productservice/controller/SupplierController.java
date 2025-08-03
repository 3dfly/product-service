package com.threedfly.productservice.controller;

import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/suppliers")
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return supplierService.findById(id)
                .map(supplier -> ResponseEntity.ok(supplier))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        Supplier saved = supplierService.save(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        Supplier updated = supplierService.save(supplier);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Supplier> getSupplierByUserId(@PathVariable Long userId) {
        return supplierService.findByUserId(userId)
                .map(supplier -> ResponseEntity.ok(supplier))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Supplier> getSupplierByEmail(@PathVariable String email) {
        return supplierService.findByEmail(email)
                .map(supplier -> ResponseEntity.ok(supplier))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/verified")
    public ResponseEntity<List<Supplier>> getVerifiedSuppliers() {
        List<Supplier> suppliers = supplierService.findVerified();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.findActive();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/verified-and-active")
    public ResponseEntity<List<Supplier>> getVerifiedAndActiveSuppliers() {
        List<Supplier> suppliers = supplierService.findVerifiedAndActive();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Supplier>> getSuppliersByCity(@PathVariable String city) {
        List<Supplier> suppliers = supplierService.findByCity(city);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/state/{state}")
    public ResponseEntity<List<Supplier>> getSuppliersByState(@PathVariable String state) {
        List<Supplier> suppliers = supplierService.findByState(state);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Supplier>> getSuppliersByCountry(@PathVariable String country) {
        List<Supplier> suppliers = supplierService.findByCountry(country);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliersByName(@RequestParam String name) {
        List<Supplier> suppliers = supplierService.searchByName(name);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<Supplier>> getSuppliersWithinRadius(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusKm) {
        List<Supplier> suppliers = supplierService.findSuppliersWithinRadius(latitude, longitude, radiusKm);
        return ResponseEntity.ok(suppliers);
    }
    
    @PostMapping("/{id}/verify")
    public ResponseEntity<Supplier> verifySupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.verifySupplier(id);
        return ResponseEntity.ok(supplier);
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Supplier> activateSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.activateSupplier(id);
        return ResponseEntity.ok(supplier);
    }
    
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Supplier> deactivateSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.deactivateSupplier(id);
        return ResponseEntity.ok(supplier);
    }
}