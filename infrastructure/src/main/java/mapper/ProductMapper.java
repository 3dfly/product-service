package mapper;

import dto.ProductRequest;
import dto.ProductResponse;
import entity.Product;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final ModelMapper modelMapper;
    
    @Value("${server.port:8081}")
    private String serverPort;

    @Autowired
    public ProductMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        
        // Map file metadata and generate URLs
        mapFileData(product, response);
        
        // Custom logic for shop name since it requires navigation to shop entity
        response.setShopName(product.getShop() != null ? product.getShop().getName() : null);
        
        return response;
    }
    
    private void mapFileData(Product product, ProductResponse response) {
        // Handle image data
        response.setHasImage(product.getImageData() != null && product.getImageData().length > 0);
        response.setImageFilename(product.getImageFilename());
        response.setImageContentType(product.getImageContentType());
        
        // Handle STL data
        response.setHasStlFile(product.getStlData() != null && product.getStlData().length > 0);
        response.setStlFilename(product.getStlFilename());
        response.setStlContentType(product.getStlContentType());
        
        // Generate URLs for stored files if they exist
        String baseUrl = getNgrokUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:" + serverPort;
        }
        
        if (response.isHasImage()) {
            response.setImageUrl(baseUrl + "/products/" + product.getId() + "/image");
        }
        
        if (response.isHasStlFile()) {
            response.setStlFileUrl(baseUrl + "/products/" + product.getId() + "/stl");
        }
    }
    
    private String getNgrokUrl() {
        // First try system property (set by NgrokAutoStartService)
        String ngrokUrl = System.getProperty("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }
        
        // Fallback to environment variable
        return System.getenv("NGROK_URL");
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