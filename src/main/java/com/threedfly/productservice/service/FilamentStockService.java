package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.FilamentStockRequest;
import com.threedfly.productservice.dto.FilamentStockResponse;
import com.threedfly.productservice.entity.FilamentStock;
import com.threedfly.productservice.entity.FilamentType;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.FilamentStockMapper;
import com.threedfly.productservice.repository.FilamentStockRepository;
import com.threedfly.productservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FilamentStockService {
    
    private final FilamentStockRepository filamentStockRepository;
    private final SupplierRepository supplierRepository;
    private final FilamentStockMapper filamentStockMapper;
    
    public List<FilamentStockResponse> findAll() {
        log.info("Finding all filament stock");
        return filamentStockRepository.findAll()
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public FilamentStockResponse findById(Long id) {
        log.info("Finding filament stock by id: {}", id);
        
        FilamentStock filamentStock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        return filamentStockMapper.toResponse(filamentStock);
    }
    
    public FilamentStockResponse save(FilamentStockRequest request) {
        log.info("Saving filament stock: {}", request);
        
        FilamentStock filamentStock = filamentStockMapper.toEntity(request);
        
        // Set supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + request.getSupplierId()));
        filamentStock.setSupplier(supplier);
        
        // Set default values
        if (filamentStock.getReservedKg() == null) {
            filamentStock.setReservedKg(0.0);
        }
        
        // Set last restocked date for new stock
        filamentStock.setLastRestocked(new Date());
        
        FilamentStock savedStock = filamentStockRepository.save(filamentStock);
        return filamentStockMapper.toResponse(savedStock);
    }
    
    public FilamentStockResponse update(Long id, FilamentStockRequest request) {
        log.info("Updating filament stock with id: {}", id);
        
        FilamentStock existingStock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        // Update fields from request
        filamentStockMapper.updateEntityFromRequest(existingStock, request);
        
        // Set supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + request.getSupplierId()));
        existingStock.setSupplier(supplier);
        
        FilamentStock savedStock = filamentStockRepository.save(existingStock);
        return filamentStockMapper.toResponse(savedStock);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting filament stock by id: {}", id);
        
        FilamentStock filamentStock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        filamentStockRepository.delete(filamentStock);
    }
    
    public List<FilamentStockResponse> findBySupplierId(Long supplierId) {
        log.info("Finding filament stock by supplier id: {}", supplierId);
        return filamentStockRepository.findBySupplierId(supplierId)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findByMaterialType(FilamentType materialType) {
        log.info("Finding filament stock by material type: {}", materialType);
        return filamentStockRepository.findByMaterialType(materialType)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findByColor(String color) {
        log.info("Finding filament stock by color: {}", color);
        return filamentStockRepository.findByColor(color)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findAvailable() {
        log.info("Finding available filament stock");
        return filamentStockRepository.findByAvailableTrue()
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findByMaterialTypeAndColor(FilamentType materialType, String color) {
        log.info("Finding filament stock by material type: {} and color: {}", materialType, color);
        return filamentStockRepository.findByMaterialTypeAndColor(materialType, color)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findStockWithSufficientQuantity(Double requiredKg) {
        log.info("Finding filament stock with sufficient quantity: {} kg", requiredKg);
        return filamentStockRepository.findStockWithSufficientQuantity(requiredKg)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findLowStockItems(Double threshold) {
        log.info("Finding low stock items below threshold: {} kg", threshold);
        return filamentStockRepository.findLowStockItems(threshold)
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<FilamentStockResponse> findExpiredStock() {
        log.info("Finding expired filament stock");
        return filamentStockRepository.findExpiredStock()
                .stream()
                .map(filamentStockMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public Long countAvailableByMaterialType(FilamentType materialType) {
        log.info("Counting available stock by material type: {}", materialType);
        return filamentStockRepository.countAvailableByMaterialType(materialType);
    }
    
    public FilamentStockResponse reserveStock(Long id, Double quantityKg) {
        log.info("Reserving {} kg from filament stock id: {}", quantityKg, id);
        
        FilamentStock stock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        if (!stock.hasEnoughStock(quantityKg)) {
            throw new RuntimeException("Insufficient stock available. Required: " + quantityKg + 
                                     ", Available: " + stock.getAvailableQuantityKg());
        }
        
        stock.setReservedKg(stock.getReservedKg() + quantityKg);
        FilamentStock savedStock = filamentStockRepository.save(stock);
        return filamentStockMapper.toResponse(savedStock);
    }
    
    public FilamentStockResponse releaseReservedStock(Long id, Double quantityKg) {
        log.info("Releasing {} kg from reserved stock id: {}", quantityKg, id);
        
        FilamentStock stock = filamentStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilamentStock not found with ID: " + id));
        
        double currentReserved = stock.getReservedKg() != null ? stock.getReservedKg() : 0.0;
        if (currentReserved < quantityKg) {
            throw new RuntimeException("Cannot release more than reserved. Reserved: " + currentReserved + 
                                     ", Requested: " + quantityKg);
        }
        
        stock.setReservedKg(currentReserved - quantityKg);
        FilamentStock savedStock = filamentStockRepository.save(stock);
        return filamentStockMapper.toResponse(savedStock);
    }

}