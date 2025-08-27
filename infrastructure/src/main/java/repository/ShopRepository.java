package repository;

import entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // Find by sellerId
    List<Shop> findBySellerId(Long sellerId);
    
    // Find single shop by sellerId (assuming one shop per seller)
    Optional<Shop> findFirstBySellerId(Long sellerId);
    
    // Search by name (case insensitive)
    @Query("SELECT s FROM Shop s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Shop> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Search by description (case insensitive)
    @Query("SELECT s FROM Shop s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Shop> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);
    
    // Search by address (case insensitive)
    @Query("SELECT s FROM Shop s WHERE LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%'))")
    List<Shop> findByAddressContainingIgnoreCase(@Param("address") String address);
    
    // Search shops with products
    @Query("SELECT DISTINCT s FROM Shop s WHERE SIZE(s.products) > 0")
    List<Shop> findShopsWithProducts();
    
    // Count products in shop
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shopId = :shopId")
    Long countProductsByShopId(@Param("shopId") Long shopId);
    
    // Check if sellerId exists
    boolean existsBySellerId(Long sellerId);
}