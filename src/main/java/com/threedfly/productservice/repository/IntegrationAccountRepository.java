package com.threedfly.productservice.repository;

import com.threedfly.productservice.entity.IntegrationAccount;
import com.threedfly.productservice.entity.ShopType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationAccountRepository extends JpaRepository<IntegrationAccount, Long> {
    Optional<IntegrationAccount> findFirstByShopIdAndProvider(Long shopId, ShopType provider);
    Optional<IntegrationAccount> findByShopIdAndProviderAndExternalShopId(Long shopId, ShopType provider, String externalShopId);
}
