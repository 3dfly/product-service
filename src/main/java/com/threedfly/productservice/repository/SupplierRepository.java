package com.threedfly.productservice.repository;

import com.threedfly.productservice.entity.Supplier;
import com.threedfly.productservice.repository.projection.SupplierWithDistanceProjection;
import com.threedfly.productservice.repository.projection.ClosetSupplierProjection;
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
    
    // Find active and verified suppliers with valid coordinates for order matching
    @Query("SELECT s FROM Supplier s WHERE s.active = true AND s.verified = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "ORDER BY s.name")
    List<Supplier> findByActiveAndVerifiedWithValidCoordinates();
    
        // Find closest supplier with stock using database spatial functions
    @Query(value = """
        SELECT DISTINCT s.id as id,
               s.user_id as userId,
               s.name as name,
               s.email as email,
               s.phone as phone,
               s.address as address,
               s.city as city,
               s.state as state,
               s.country as country,
               s.postal_code as postalCode,
               s.latitude as latitude,
               s.longitude as longitude,
               s.business_license as businessLicense,
               s.description as description,
               s.verified as verified,
               s.active as active,
               6371 * acos(cos(radians(:buyerLat)) * cos(radians(s.latitude)) *
                           cos(radians(s.longitude) - radians(:buyerLon)) +
                           sin(radians(:buyerLat)) * sin(radians(s.latitude))) as distanceKm
        FROM supplier s
        INNER JOIN filament_stock fs ON s.id = fs.supplier_id
        WHERE s.active = true
          AND s.verified = true
          AND s.latitude IS NOT NULL
          AND s.longitude IS NOT NULL
          AND fs.material_type = :materialType
          AND fs.color = :color
          AND fs.available = true
          AND (fs.quantity_kg - COALESCE(fs.reserved_kg, 0.0)) >= :requiredQuantity
        ORDER BY distanceKm ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<SupplierWithDistanceProjection> findClosestSupplierWithStock(@Param("buyerLat") Double buyerLatitude,
                                                                          @Param("buyerLon") Double buyerLongitude,
                                                                          @Param("materialType") String materialType,
                                                                          @Param("color") String color,
                                                                          @Param("requiredQuantity") Double requiredQuantity);

    // Optimized version that also returns stock information in a single query
    @Query(value = """
        SELECT s.id as id,
               s.user_id as userId,
               s.name as name,
               s.email as email,
               s.phone as phone,
               s.address as address,
               s.city as city,
               s.state as state,
               s.country as country,
               s.postal_code as postalCode,
               s.latitude as latitude,
               s.longitude as longitude,
               s.business_license as businessLicense,
               s.description as description,
               s.verified as verified,
               s.active as active,
               fs.id as stockId,
               fs.material_type as materialType,
               fs.color as color,
               fs.quantity_kg as quantityKg,
               fs.reserved_kg as reservedKg,
               (fs.quantity_kg - COALESCE(fs.reserved_kg, 0.0)) as availableQuantityKg,
               fs.available as available,
               6371 * acos(cos(radians(:buyerLat)) * cos(radians(s.latitude)) *
                           cos(radians(s.longitude) - radians(:buyerLon)) +
                           sin(radians(:buyerLat)) * sin(radians(s.latitude))) as distanceKm
        FROM supplier s
        INNER JOIN filament_stock fs ON s.id = fs.supplier_id
        WHERE s.active = true
          AND s.verified = true
          AND s.latitude IS NOT NULL
          AND s.longitude IS NOT NULL
          AND fs.material_type = :materialType
          AND fs.color = :color
          AND fs.available = true
          AND (fs.quantity_kg - COALESCE(fs.reserved_kg, 0.0)) >= :requiredQuantity
        ORDER BY distanceKm ASC, (fs.quantity_kg - COALESCE(fs.reserved_kg, 0.0)) DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<ClosetSupplierProjection> findClosestSupplierWithStockOptimized(@Param("buyerLat") Double buyerLatitude,
                                                                             @Param("buyerLon") Double buyerLongitude,
                                                                             @Param("materialType") String materialType,
                                                                             @Param("color") String color,
                                                                             @Param("requiredQuantity") Double requiredQuantity);
}