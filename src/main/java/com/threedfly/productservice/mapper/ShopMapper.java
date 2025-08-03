package com.threedfly.productservice.mapper;

import com.threedfly.productservice.dto.ShopRequest;
import com.threedfly.productservice.dto.ShopResponse;
import com.threedfly.productservice.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {

    public ShopResponse toResponse(Shop shop) {
        if (shop == null) {
            return null;
        }

        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setSellerId(shop.getSellerId());
        response.setName(shop.getName());
        response.setDescription(shop.getDescription());
        response.setAddress(shop.getAddress());
        response.setContactInfo(shop.getContactInfo());
        response.setProductCount(shop.getProducts() != null ? (long) shop.getProducts().size() : 0L);

        return response;
    }

    public Shop toEntity(ShopRequest request) {
        if (request == null) {
            return null;
        }

        Shop shop = new Shop();
        shop.setSellerId(request.getSellerId());
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setContactInfo(request.getContactInfo());

        return shop;
    }

    public void updateEntityFromRequest(Shop shop, ShopRequest request) {
        if (shop == null || request == null) {
            return;
        }

        shop.setSellerId(request.getSellerId());
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setContactInfo(request.getContactInfo());
    }
}