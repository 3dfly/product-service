package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.ProductRequest;
import com.threedfly.productservice.dto.ProductResponse;
import com.threedfly.productservice.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImageUrl(product.getImageUrl());
        response.setStlFileUrl(product.getStlFileUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setSellerId(product.getSellerId());
        response.setShopId(product.getShopId());
        response.setShopName(product.getShop() != null ? product.getShop().getName() : null);

        return response;
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStlFileUrl(request.getStlFileUrl());
        product.setSellerId(request.getSellerId());
        product.setShopId(request.getShopId());

        return product;
    }

    public void updateEntityFromRequest(Product product, ProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStlFileUrl(request.getStlFileUrl());
        product.setSellerId(request.getSellerId());
        product.setShopId(request.getShopId());
    }
}