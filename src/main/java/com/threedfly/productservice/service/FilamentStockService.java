package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.FilamentStockRepository;
import com.threedfly.productservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FilamentStockService {
    
    private final FilamentStockRepository filamentStockRepository;
    private final SupplierRepository supplierRepository;
    
    public List<FilamentStock> findAll() {
        log.info("Finding all filament stock");
        return filamentStockRepository.findAll();
    }
    
    public Optional<FilamentStock> findById(Long id) {
        log.info("Finding filament stock by id: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        return filamentStockRepository.findById(id);
    }
    
    public FilamentStock save(FilamentStock filamentStock) {
        log.info("Saving filament stock: {}", filamentStock);
        if (filamentStock == null) {
            throw new IllegalArgumentException("FilamentStock cannot be null");
        }
        
        // Validate supplier exists if supplier ID is provided
        if (filamentStock.getSupplier() != null && filamentStock.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(filamentStock.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + filamentStock.getSupplier().getId()));
            filamentStock.setSupplier(supplier);
        }
        
        // Set default values
        if (filamentStock.getReservedKg() == null) {
            filamentStock.setReservedKg(0.0);
        }
        
        // Set last restocked date for new stock or when quantity increases
        if (filamentStock.getId() == null || 
            (filamentStock.getId() != null && hasQuantityIncreased(filamentStock))) {
            filamentStock.setLastRestocked(new Date());
        }
        
        return filamentStockRepository.save(filamentStock);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting filament stock by id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        if (!filamentStockRepository.existsById(id)) {
            throw new RuntimeException("FilamentStock not found with ID: " + id);
        }
        
        filamentStockRepository.deleteById(id);
    }
    
    public List<FilamentStock> findBySupplierId(Long supplierId) {
        log.info("Finding filament stock by supplier id: {}", supplierId);
        if (supplierId == null) {
            throw new IllegalArgumentException("Supplier ID cannot be null");
        }
        return filamentStockRepository.findBySupplierId(supplierId);
    }
    
    public List<FilamentStock> findByMaterialType(FilamentType materialType) {
        log.info("Finding filament stock by material type: {}", materialType);
        if (materialType == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return filamentStockRepository.findByMaterialType(materialType);
    }
    
    public List<FilamentStock> findByColor(String color) {
        log.info("Finding filament stock by color: {}", color);
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        return filamentStockRepository.findByColor(color);
    }
    
    public List<FilamentStock> findAvailable() {
        log.info("Finding available filament stock");
        return filamentStockRepository.findByAvailableTrue();
    }
    
    public List<FilamentStock> findByMaterialTypeAndColor(FilamentType materialType, String color) {
        log.info("Finding filament stock by material type: {} and color: {}", materialType, color);
        if (materialType == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        return filamentStockRepository.findByMaterialTypeAndColor(materialType, color);
    }
    
    public List<FilamentStock> findStockWithSufficientQuantity(Double requiredKg) {
        log.info("Finding filament stock with sufficient quantity: {} kg", requiredKg);
        if (requiredKg == null || requiredKg < 0) {
            throw new IllegalArgumentException("Required quantity must be non-negative");
        }
        return filamentStockRepository.findStockWithSufficientQuantity(requiredKg);
    }
    
    public List<FilamentStock> findLowStockItems(Double threshold) {
        log.info("Finding low stock items below threshold: {} kg", threshold);
        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("Threshold must be non-negative");
        }
        return filamentStockRepository.findLowStockItems(threshold);
    }
    
    public List<FilamentStock> findExpiredStock() {
        log.info("Finding expired filament stock");
        return filamentStockRepository.findExpiredStock();
    }
    
    public Long countAvailableByMaterialType(FilamentType materialType) {
        log.info("Counting available stock by material type: {}", materialType);
        if (materialType == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return filamentStockRepository.countAvailableByMaterialType(materialType);
    }
    
    public FilamentStock reserveStock(Long id, Double quantityKg) {
        log.info("Reserving {} kg from filament stock id: {}", quantityKg, id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (quantityKg == null || quantityKg <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        FilamentStock stock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        if (!stock.hasEnoughStock(quantityKg)) {
            throw new RuntimeException("Insufficient stock available. Required: " + quantityKg + 
                                     ", Available: " + stock.getAvailableQuantityKg());
        }
        
        stock.setReservedKg(stock.getReservedKg() + quantityKg);
        return filamentStockRepository.save(stock);
    }
    
    public FilamentStock releaseReservedStock(Long id, Double quantityKg) {
        log.info("Releasing {} kg from reserved stock id: {}", quantityKg, id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (quantityKg == null || quantityKg <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        FilamentStock stock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        double currentReserved = stock.getReservedKg() != null ? stock.getReservedKg() : 0.0;
        if (currentReserved < quantityKg) {
            throw new RuntimeException("Cannot release more than reserved. Reserved: " + currentReserved + 
                                     ", Requested: " + quantityKg);
        }
        
        stock.setReservedKg(currentReserved - quantityKg);
        return filamentStockRepository.save(stock);
    }
    
    private boolean hasQuantityIncreased(FilamentStock filamentStock) {
        if (filamentStock.getId() == null) {
            return true; // New stock
        }
        
        Optional<FilamentStock> existing = filamentStockRepository.findById(filamentStock.getId());
        if (existing.isPresent()) {
            Double existingQuantity = existing.get().getQuantityKg();
            Double newQuantity = filamentStock.getQuantityKg();
            return newQuantity != null && existingQuantity != null && newQuantity > existingQuantity;
        }
        
        return false;
    }
}