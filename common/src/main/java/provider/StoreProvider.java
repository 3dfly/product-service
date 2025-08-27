package provider;

import dto.PublishToStoreRequest;
import entity.IntegrationAccount;
import entity.Product;
import entity.ProductSync;
import org.springframework.http.ResponseEntity;

public interface StoreProvider {
    ResponseEntity<?> publishProduct(PublishToStoreRequest req, Product product, IntegrationAccount integrationAccount) throws Exception;

    ResponseEntity<?> deleteProduct(Product product, ProductSync syncRecord, IntegrationAccount account);
}