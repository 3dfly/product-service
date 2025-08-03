package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.ProductRequest;
import com.threedfly.productservice.dto.ProductResponse;
import com.threedfly.productservice.entity.Product;
import com.threedfly.productservice.mapper.ProductMapper;
import com.threedfly.productservice.repository.ProductRepository;
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
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
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
} 