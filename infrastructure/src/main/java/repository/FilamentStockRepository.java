package repository;

import entity.FilamentStock;
import entity.FilamentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilamentStockRepository extends JpaRepository<FilamentStock, Long> {
    
    // Find by supplier
    List<FilamentStock> findBySupplierId(Long supplierId);
    
    // Find by material type
    List<FilamentStock> findByMaterialType(FilamentType materialType);
    
    // Find by color
    List<FilamentStock> findByColor(String color);
    
    // Find available stock
    List<FilamentStock> findByAvailableTrue();
    
    // Find by material type and color
    List<FilamentStock> findByMaterialTypeAndColor(FilamentType materialType, String color);

    // Find stock with sufficient quantity
    @Query("SELECT f FROM FilamentStock f WHERE (f.quantityKg - COALESCE(f.reservedKg, 0.0)) >= :requiredKg AND f.available = true")
    List<FilamentStock> findStockWithSufficientQuantity(@Param("requiredKg") Double requiredKg);
    
    // Find low stock items (less than threshold)
    @Query("SELECT f FROM FilamentStock f WHERE (f.quantityKg - COALESCE(f.reservedKg, 0.0)) < :threshold AND f.available = true")
    List<FilamentStock> findLowStockItems(@Param("threshold") Double threshold);
    
    // Find expired stock
    @Query("SELECT f FROM FilamentStock f WHERE f.expiryDate < CURRENT_DATE")
    List<FilamentStock> findExpiredStock();
    
    // Count available stock by material type
    @Query("SELECT COUNT(f) FROM FilamentStock f WHERE f.materialType = :materialType AND f.available = true")
    Long countAvailableByMaterialType(@Param("materialType") FilamentType materialType);
}