package com.threedfly.productservice.repository;

import com.threedfly.productservice.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    // Find by userId
    Optional<Supplier> findByUserId(Long userId);
    
    // Find by email
    Optional<Supplier> findByEmail(String email);
    
    // Find verified suppliers
    List<Supplier> findByVerifiedTrue();
    
    // Find active suppliers
    List<Supplier> findByActiveTrue();
    
    // Find verified and active suppliers
    List<Supplier> findByVerifiedTrueAndActiveTrue();
    
    // Find by city
    List<Supplier> findByCity(String city);
    
    // Find by state
    List<Supplier> findByState(String state);
    
    // Find by country
    List<Supplier> findByCountry(String country);
    
    // Search by name (case insensitive)
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Supplier> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find suppliers within distance from coordinates
    @Query("SELECT s FROM Supplier s WHERE " +
           "SQRT(POWER((s.latitude - :latitude) * 111.0, 2) + " +
           "POWER((s.longitude - :longitude) * 111.0 * COS(RADIANS(:latitude)), 2)) <= :radiusKm " +
           "AND s.active = true AND s.verified = true")
    List<Supplier> findSuppliersWithinRadius(@Param("latitude") Double latitude, 
                                            @Param("longitude") Double longitude, 
                                            @Param("radiusKm") Double radiusKm);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if userId exists
    boolean existsByUserId(Long userId);
}