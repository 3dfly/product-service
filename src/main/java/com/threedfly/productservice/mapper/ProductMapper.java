package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.ProductRequest;
import com.threedfly.productservice.dto.ProductResponse;
import com.threedfly.productservice.entity.Product;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public ProductMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        // Custom logic for shop name since it requires navigation to shop entity
        response.setShopName(product.getShop() != null ? product.getShop().getName() : null);
        
        return response;
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        return modelMapper.map(request, Product.class);
    }

    public void updateEntityFromRequest(Product product, ProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        modelMapper.map(request, product);
    }
}