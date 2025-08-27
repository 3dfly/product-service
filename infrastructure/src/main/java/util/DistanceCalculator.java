package util;

/**
 * Utility class for calculating geographic distances using the Haversine formula.
 * The Haversine formula determines the great-circle distance between two points 
 * on a sphere given their latitude and longitude.
 */
public class DistanceCalculator {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate the distance between two geographic points using the Haversine formula.
     * 
     * @param lat1 Latitude of the first point in degrees
     * @param lon1 Longitude of the first point in degrees
     * @param lat2 Latitude of the second point in degrees
     * @param lon2 Longitude of the second point in degrees
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }
        
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Calculate differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        
        // Apply Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Round distance to specified decimal places.
     * 
     * @param distance The distance to round
     * @param decimalPlaces Number of decimal places
     * @return Rounded distance
     */
    public static double roundDistance(double distance, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(distance * multiplier) / multiplier;
    }
}