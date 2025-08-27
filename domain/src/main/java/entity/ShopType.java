package entity;

import lombok.Getter;

@Getter
public enum ShopType {
    SHOPIFY("shopify");

    // getter
    private final String value;

    // constructor
    ShopType(String value) {
        this.value = value;
    }

}
