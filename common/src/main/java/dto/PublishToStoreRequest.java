package dto;

import entity.ShopType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublishToStoreRequest {
    private String title;

    private String description;

    private BigDecimal price;

    private String status;

    private Long integrationAccountId;

    private ShopType shopType;
}
