package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.SupplierRequest;
import com.threedfly.productservice.dto.SupplierResponse;
import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.mapper.SupplierMapper;
import com.threedfly.productservice.repository.SupplierRepository;
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
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    
    public List<SupplierResponse> findAll() {
        log.info("Finding all suppliers");
        return supplierRepository.findAll()
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public SupplierResponse findById(Long id) {
        log.info("Finding supplier by id: {}", id);
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        return supplierMapper.toResponse(supplier);
    }
    
    public SupplierResponse save(SupplierRequest request) {
        log.info("Saving supplier: {}", request);
        
        // Check for duplicate email
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Supplier with email " + request.getEmail() + " already exists");
        }
        
        // Check for duplicate userId
        if (request.getUserId() != null && supplierRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Supplier with userId " + request.getUserId() + " already exists");
        }
        
        Supplier supplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting supplier by id: {}", id);
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplierRepository.delete(supplier);
    }
    
    public SupplierResponse findByUserId(Long userId) {
        log.info("Finding supplier by user id: {}", userId);
        
        Supplier supplier = supplierRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with user ID: " + userId));
        
        return supplierMapper.toResponse(supplier);
    }
    
    public SupplierResponse findByEmail(String email) {
        log.info("Finding supplier by email: {}", email);
        
        Supplier supplier = supplierRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Supplier not found with email: " + email));
        
        return supplierMapper.toResponse(supplier);
    }
    
    public List<SupplierResponse> findVerified() {
        log.info("Finding verified suppliers");
        return supplierRepository.findByVerifiedTrue()
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findActive() {
        log.info("Finding active suppliers");
        return supplierRepository.findByActiveTrue()
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findVerifiedAndActive() {
        log.info("Finding verified and active suppliers");
        return supplierRepository.findByVerifiedTrueAndActiveTrue()
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findByCity(String city) {
        log.info("Finding suppliers by city: {}", city);
        return supplierRepository.findByCity(city)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findByState(String state) {
        log.info("Finding suppliers by state: {}", state);
        return supplierRepository.findByState(state)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findByCountry(String country) {
        log.info("Finding suppliers by country: {}", country);
        return supplierRepository.findByCountry(country)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> searchByName(String name) {
        log.info("Searching suppliers by name: {}", name);
        return supplierRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SupplierResponse> findSuppliersWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        log.info("Finding suppliers within {} km of coordinates: {}, {}", radiusKm, latitude, longitude);
        
        return supplierRepository.findSuppliersWithinRadius(latitude, longitude, radiusKm)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public SupplierResponse verifySupplier(Long id) {
        log.info("Verifying supplier with id: {}", id);
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setVerified(true);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }
    
    public SupplierResponse activateSupplier(Long id) {
        log.info("Activating supplier with id: {}", id);
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setActive(true);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }
    
    public SupplierResponse deactivateSupplier(Long id) {
        log.info("Deactivating supplier with id: {}", id);
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setActive(false);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }
    
    public SupplierResponse update(Long id, SupplierRequest request) {
        log.info("Updating supplier with id: {}", id);
        
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        // Check for duplicate email if email is being changed
        if (!existingSupplier.getEmail().equals(request.getEmail()) && 
            supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Supplier with email " + request.getEmail() + " already exists");
        }
        
        // Check for duplicate userId if userId is being changed
        if (request.getUserId() != null && 
            !java.util.Objects.equals(existingSupplier.getUserId(), request.getUserId()) &&
            supplierRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Supplier with userId " + request.getUserId() + " already exists");
        }
        
        supplierMapper.updateEntityFromRequest(existingSupplier, request);
        Supplier savedSupplier = supplierRepository.save(existingSupplier);
        return supplierMapper.toResponse(savedSupplier);
    }
    

}