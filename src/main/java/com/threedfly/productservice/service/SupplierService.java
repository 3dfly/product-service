package com.threedfly.productservice.service;

import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    public List<Supplier> findAll() {
        log.info("Finding all suppliers");
        return supplierRepository.findAll();
    }
    
    public Optional<Supplier> findById(Long id) {
        log.info("Finding supplier by id: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        return supplierRepository.findById(id);
    }
    
    public Supplier save(Supplier supplier) {
        log.info("Saving supplier: {}", supplier);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        
        // Validate required fields
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        
        if (supplier.getEmail() == null || supplier.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier email is required");
        }
        
        // Check for duplicate email (only for new suppliers or when email is changed)
        if (supplier.getId() == null || isEmailChanged(supplier)) {
            if (supplierRepository.existsByEmail(supplier.getEmail())) {
                throw new RuntimeException("Supplier with email " + supplier.getEmail() + " already exists");
            }
        }
        
        // Check for duplicate userId (only for new suppliers or when userId is changed)
        if (supplier.getUserId() != null && (supplier.getId() == null || isUserIdChanged(supplier))) {
            if (supplierRepository.existsByUserId(supplier.getUserId())) {
                throw new RuntimeException("Supplier with userId " + supplier.getUserId() + " already exists");
            }
        }
        
        return supplierRepository.save(supplier);
    }
    
    public void deleteById(Long id) {
        log.info("Deleting supplier by id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with ID: " + id);
        }
        
        supplierRepository.deleteById(id);
    }
    
    public Optional<Supplier> findByUserId(Long userId) {
        log.info("Finding supplier by user id: {}", userId);
        if (userId == null) {
            return Optional.empty();
        }
        return supplierRepository.findByUserId(userId);
    }
    
    public Optional<Supplier> findByEmail(String email) {
        log.info("Finding supplier by email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return supplierRepository.findByEmail(email);
    }
    
    public List<Supplier> findVerified() {
        log.info("Finding verified suppliers");
        return supplierRepository.findByVerifiedTrue();
    }
    
    public List<Supplier> findActive() {
        log.info("Finding active suppliers");
        return supplierRepository.findByActiveTrue();
    }
    
    public List<Supplier> findVerifiedAndActive() {
        log.info("Finding verified and active suppliers");
        return supplierRepository.findByVerifiedTrueAndActiveTrue();
    }
    
    public List<Supplier> findByCity(String city) {
        log.info("Finding suppliers by city: {}", city);
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        return supplierRepository.findByCity(city);
    }
    
    public List<Supplier> findByState(String state) {
        log.info("Finding suppliers by state: {}", state);
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }
        return supplierRepository.findByState(state);
    }
    
    public List<Supplier> findByCountry(String country) {
        log.info("Finding suppliers by country: {}", country);
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
        return supplierRepository.findByCountry(country);
    }
    
    public List<Supplier> searchByName(String name) {
        log.info("Searching suppliers by name: {}", name);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Supplier> findSuppliersWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        log.info("Finding suppliers within {} km of coordinates: {}, {}", radiusKm, latitude, longitude);
        if (latitude == null || longitude == null || radiusKm == null) {
            throw new IllegalArgumentException("Latitude, longitude, and radius cannot be null");
        }
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        
        return supplierRepository.findSuppliersWithinRadius(latitude, longitude, radiusKm);
    }
    
    public Supplier verifySupplier(Long id) {
        log.info("Verifying supplier with id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setVerified(true);
        return supplierRepository.save(supplier);
    }
    
    public Supplier activateSupplier(Long id) {
        log.info("Activating supplier with id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }
    
    public Supplier deactivateSupplier(Long id) {
        log.info("Deactivating supplier with id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));
        
        supplier.setActive(false);
        return supplierRepository.save(supplier);
    }
    
    private boolean isEmailChanged(Supplier supplier) {
        if (supplier.getId() == null) {
            return true; // New supplier
        }
        
        Optional<Supplier> existing = supplierRepository.findById(supplier.getId());
        if (existing.isPresent()) {
            String existingEmail = existing.get().getEmail();
            String newEmail = supplier.getEmail();
            return !existingEmail.equals(newEmail);
        }
        
        return false;
    }
    
    private boolean isUserIdChanged(Supplier supplier) {
        if (supplier.getId() == null) {
            return true; // New supplier
        }
        
        Optional<Supplier> existing = supplierRepository.findById(supplier.getId());
        if (existing.isPresent()) {
            Long existingUserId = existing.get().getUserId();
            Long newUserId = supplier.getUserId();
            return !java.util.Objects.equals(existingUserId, newUserId);
        }
        
        return false;
    }
}