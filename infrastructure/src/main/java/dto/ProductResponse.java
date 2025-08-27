package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    
    // Image file information
    private String imageFilename;
    private String imageContentType;
    private boolean hasImage;  // Indicates if image data exists
    
    // STL file information  
    private String stlFilename;
    private String stlContentType;
    private boolean hasStlFile;  // Indicates if STL data exists
    
    // URLs for accessing stored files (generated dynamically)
    private String imageUrl;      // Generated URL to access stored image
    private String stlFileUrl;    // Generated URL to access stored STL file
    
    private Long sellerId;
    private Long shopId;
    private String shopName;  // Navigation to shop entity
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
