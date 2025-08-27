package service;

import dto.ProductRequest;
import dto.ProductResponse;
import dto.PublishToStoreRequest;
import entity.IntegrationAccount;
import entity.Product;
import entity.ProductSync;
import entity.ShopType;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.ProductMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import provider.StoreProvider;
import repository.IntegrationAccountRepository;
import repository.ProductRepository;
import repository.ProductSyncRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ApplicationContext applicationContext;
    private final IntegrationAccountRepository integrationAccountRepository;
    private final ProductSyncRepository productSyncRepository;

    public ResponseEntity<?> publishProduct(Long id, PublishToStoreRequest req) throws Exception {
        // Get the product with stored files
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        System.out.println("🖼️ Publishing product with stored files: " + product.getName());

        // 1) Resolve integration account
        var integrationAccount = integrationAccountRepository.findById(req.getIntegrationAccountId())
                .orElseThrow(() -> new IllegalArgumentException("integrationAccountId not found: " + req.getIntegrationAccountId()));

        var storeProvider = applicationContext.getBean(integrationAccount.getProvider().name(), StoreProvider.class);

        return storeProvider.publishProduct(req, product, integrationAccount);
    }

    public List<ProductResponse> findAll() {
        log.info("Finding all products");
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse findById(Long id) {
        log.info("Finding product by id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        return productMapper.toResponse(product);
    }
    
    public ProductResponse save(ProductRequest request) {
        log.info("Saving product: {}", request);
        
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        productMapper.updateEntityFromRequest(existingProduct, request);
        Product savedProduct = productRepository.save(existingProduct);
        
        return productMapper.toResponse(savedProduct);
    }
    
    public void delete(Long id) {
        log.info("Deleting product by id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        productRepository.delete(product);
    }

    @Transactional
    public ResponseEntity<?> deleteProduct(Long id, ShopType shopType) {

        System.out.println("🗑️ Starting deletion of product " + id + " from Shopify and local database");

        // 1) Get the product to find its Shopify ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // 2) Find the ProductSync record to get the Shopify product ID
        ProductSync syncRecord = productSyncRepository.findByProductId(id)
                .orElseThrow(() -> new RuntimeException("Sync record not found with ID: " + id));

        // 3) Get integration account details
        IntegrationAccount account = integrationAccountRepository.findById(syncRecord.getIntegrationAccountId())
                .orElseThrow(() -> new RuntimeException("Integration account not found: " + syncRecord.getIntegrationAccountId()));

        var storeProvider = applicationContext.getBean(shopType.getValue(), StoreProvider.class);

        var result = storeProvider.deleteProduct(product, syncRecord, account);

        // 5) Delete the sync record
        productSyncRepository.delete(syncRecord);
        System.out.println("🗑️ Deleted ProductSync record");

        // 6) Delete from local database
        delete(id);
        System.out.println("🗑️ Deleted from local database");

        return result;
    }
}