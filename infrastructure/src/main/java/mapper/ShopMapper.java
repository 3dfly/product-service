package mapper;

import dto.ShopRequest;
import dto.ShopResponse;
import entity.Shop;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public ShopMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ShopResponse toResponse(Shop shop) {
        if (shop == null) {
            return null;
        }

        ShopResponse response = modelMapper.map(shop, ShopResponse.class);
        // Custom logic for product count since it's not a direct field mapping
        response.setProductCount(shop.getProducts() != null ? (long) shop.getProducts().size() : 0L);
        
        return response;
    }

    public Shop toEntity(ShopRequest request) {
        if (request == null) {
            return null;
        }

        return modelMapper.map(request, Shop.class);
    }

    public void updateEntityFromRequest(Shop shop, ShopRequest request) {
        if (shop == null || request == null) {
            return;
        }

        modelMapper.map(request, shop);
    }
}