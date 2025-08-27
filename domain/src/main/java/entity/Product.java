package entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    private String description;

    private double price;

    // Image storage
    @Lob
    @Column(name = "image_data")
    private byte[] imageData;
    
    @Column(name = "image_filename")
    private String imageFilename;
    
    @Column(name = "image_content_type")
    private String imageContentType;

    // STL file storage
    @Lob
    @Column(name = "stl_data")
    private byte[] stlData;
    
    @Column(name = "stl_filename")
    private String stlFilename;
    
    @Column(name = "stl_content_type")
    private String stlContentType;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long sellerId; // Reference to Seller in order-service
    private Long shopId;   // Reference to Shop in this service

    @ManyToOne
    @JoinColumn(name = "shopId", insertable = false, updatable = false)
    private Shop shop;
} 