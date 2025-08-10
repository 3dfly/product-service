package com.threedfly.productservice.service;

import com.threedfly.productservice.dto.GeocodingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final WebClient webClient;
    
    @Value("${geocoding.timeout:5000}")
    private long timeoutMs;
    
    @Value("${geocoding.enabled:true}")
    private boolean geocodingEnabled;

    /**
     * Geocodes an address to get latitude and longitude coordinates.
     * Uses OpenStreetMap Nominatim API as it's free and doesn't require API keys.
     * 
     * @param address The address to geocode
     * @return GeocodingResponse with coordinates or error information
     */
    public GeocodingResponse geocodeAddress(String address) {
        if (!geocodingEnabled) {
            log.warn("Geocoding is disabled, cannot geocode address: {}", address);
            return GeocodingResponse.failure("Geocoding service is disabled");
        }
        
        if (address == null || address.trim().isEmpty()) {
            return GeocodingResponse.failure("Address is null or empty");
        }

        try {
            log.info("Geocoding address: {}", address);
            
            String encodedAddress = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search" +
                    "?q=" + encodedAddress +
                    "&format=json" +
                    "&limit=1" +
                    "&addressdetails=1";

            NominatimResponse[] responses = webClient.get()
                    .uri(url)
                    .header("User-Agent", "ProductService/1.0 (geocoding for supplier matching)")
                    .retrieve()
                    .bodyToMono(NominatimResponse[].class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (responses == null || responses.length == 0) {
                log.warn("No geocoding results found for address: {}", address);
                return GeocodingResponse.failure("No geocoding results found for the provided address");
            }

            NominatimResponse response = responses[0];
            double latitude = Double.parseDouble(response.getLat());
            double longitude = Double.parseDouble(response.getLon());
            
            log.info("Successfully geocoded address '{}' to coordinates: {}, {}", 
                    address, latitude, longitude);
            
            return GeocodingResponse.success(latitude, longitude, response.getDisplayName());
            
        } catch (WebClientException e) {
            log.error("Network error during geocoding for address '{}': {}", address, e.getMessage());
            return GeocodingResponse.failure("Network error during geocoding: " + e.getMessage());
        } catch (NumberFormatException e) {
            log.error("Invalid coordinate format in geocoding response for address '{}': {}", address, e.getMessage());
            return GeocodingResponse.failure("Invalid coordinate format in response");
        } catch (Exception e) {
            log.error("Unexpected error during geocoding for address '{}': {}", address, e.getMessage(), e);
            return GeocodingResponse.failure("Unexpected error during geocoding: " + e.getMessage());
        }
    }

    /**
     * Checks if both latitude and longitude are null (coordinates missing)
     */
    public boolean areCoordinatesMissing(Double latitude, Double longitude) {
        return latitude == null && longitude == null;
    }

    /**
     * Response structure for OpenStreetMap Nominatim API
     */
    private static class NominatimResponse {
        private String lat;
        private String lon;
        private String display_name;

        // Getters and setters
        public String getLat() { return lat; }
        public void setLat(String lat) { this.lat = lat; }
        
        public String getLon() { return lon; }
        public void setLon(String lon) { this.lon = lon; }
        
        public String getDisplayName() { return display_name; }
        public void setDisplay_name(String display_name) { this.display_name = display_name; }
    }
}
