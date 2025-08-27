package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private String address;
    private String contactInfo;
    private Long productCount;
}