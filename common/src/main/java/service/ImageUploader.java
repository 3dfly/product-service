package service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploader {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:8081}")
    private String serverPort;

    public List<String> processUploadedImages(List<MultipartFile> images) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image: " + image.getOriginalFilename());
            }

            // Generate unique filename
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generate URL - use ngrok URL if available, otherwise localhost
            String baseUrl = getNgrokUrl();
            System.out.println("🔍 NGROK_URL check: " + baseUrl);
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "http://localhost:" + serverPort;
                System.out.println("📍 Using localhost URL: " + baseUrl);
            } else {
                System.out.println("🌐 Using ngrok URL: " + baseUrl);
            }
            String imageUrl = baseUrl + "/images/" + fileName;
            System.out.println("🔗 Final image URL: " + imageUrl);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    private String getNgrokUrl() {
        // First check system property (set by NgrokAutoStartService)
        String ngrokUrl = System.getProperty("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }
        
        // Fallback to environment variable
        ngrokUrl = System.getenv("NGROK_URL");
        if (ngrokUrl != null && !ngrokUrl.isEmpty()) {
            return ngrokUrl;
        }
        
        return null;
    }

}
