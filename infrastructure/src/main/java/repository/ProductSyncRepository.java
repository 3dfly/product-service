package repository;

import entity.ProductSync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductSyncRepository extends JpaRepository<ProductSync, Long> {
    Optional<ProductSync> findByProductIdAndIntegrationAccountId(Long productId, Long integrationAccountId);
    Optional<ProductSync> findByProductId(Long productId);
}
