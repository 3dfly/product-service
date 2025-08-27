package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId; // Reference to Seller in order-service
    private String name;
    private String description;
    private String address;
    private String contactInfo;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products;
}
