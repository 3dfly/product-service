package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {
    private Double latitude;
    private Double longitude;
    private String formattedAddress;
    private boolean success;
    private String errorMessage;
    
    public static GeocodingResponse success(Double latitude, Double longitude, String formattedAddress) {
        return new GeocodingResponse(latitude, longitude, formattedAddress, true, null);
    }
    
    public static GeocodingResponse failure(String errorMessage) {
        return new GeocodingResponse(null, null, null, false, errorMessage);
    }
}
