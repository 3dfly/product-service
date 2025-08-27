package com.threedfly.productservice.util;

import org.junit.jupiter.api.Test;
import util.DistanceCalculator;

import static org.junit.jupiter.api.Assertions.*;

class DistanceCalculatorTest {

    @Test
    void calculateDistance_WhenSameLocation_ShouldReturnZero() {
        // Given
        double lat = 34.0522;
        double lon = -118.2437;

        // When
        double distance = DistanceCalculator.calculateDistance(lat, lon, lat, lon);

        // Then
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void calculateDistance_WhenKnownDistances_ShouldReturnCorrectValues() {
        // Given - Los Angeles to San Francisco (approximately 560 km)
        double laLat = 34.0522;
        double laLon = -118.2437;
        double sfLat = 37.7749;
        double sfLon = -122.4194;

        // When
        double distance = DistanceCalculator.calculateDistance(laLat, laLon, sfLat, sfLon);

        // Then - Should be approximately 559 km (allow 10% tolerance for spherical calculation)
        assertTrue(distance > 500, "Distance should be greater than 500 km");
        assertTrue(distance < 620, "Distance should be less than 620 km");
    }

    @Test
    void calculateDistance_WhenNewYorkToLondon_ShouldReturnCorrectValue() {
        // Given - New York to London (approximately 5585 km)
        double nyLat = 40.7128;
        double nyLon = -74.0060;
        double londonLat = 51.5074;
        double londonLon = -0.1278;

        // When
        double distance = DistanceCalculator.calculateDistance(nyLat, nyLon, londonLat, londonLon);

        // Then - Should be approximately 5585 km (allow 10% tolerance)
        assertTrue(distance > 5000, "Distance should be greater than 5000 km");
        assertTrue(distance < 6000, "Distance should be less than 6000 km");
    }

    @Test
    void calculateDistance_WhenShortDistance_ShouldBeAccurate() {
        // Given - Two points about 1 degree apart (approximately 111 km)
        double lat1 = 34.0522;
        double lon1 = -118.2437;
        double lat2 = 35.0522; // 1 degree north
        double lon2 = -118.2437;

        // When
        double distance = DistanceCalculator.calculateDistance(lat1, lon1, lat2, lon2);

        // Then - Should be approximately 111 km (1 degree latitude ≈ 111 km)
        assertTrue(distance > 100, "Distance should be greater than 100 km");
        assertTrue(distance < 125, "Distance should be less than 125 km");
    }

    @Test
    void calculateDistance_WhenCrossingPrimeMeridian_ShouldWorkCorrectly() {
        // Given - Points on either side of the Prime Meridian
        double lat1 = 51.5074; // London
        double lon1 = -0.1278;
        double lat2 = 51.5074; // Point east of London
        double lon2 = 0.1278;

        // When
        double distance = DistanceCalculator.calculateDistance(lat1, lon1, lat2, lon2);

        // Then - Should be a small positive distance
        assertTrue(distance > 0, "Distance should be positive");
        assertTrue(distance < 50, "Distance should be less than 50 km for such close points");
    }

    @Test
    void calculateDistance_WhenCrossingDateLine_ShouldWorkCorrectly() {
        // Given - Points on either side of the International Date Line
        double lat1 = 61.2181; // Anchorage, Alaska
        double lon1 = -149.9003;
        double lat2 = 64.0685; // Fairbanks, Alaska
        double lon2 = -147.7208;

        // When
        double distance = DistanceCalculator.calculateDistance(lat1, lon1, lat2, lon2);

        // Then - Should be approximately 359 km
        assertTrue(distance > 300, "Distance should be greater than 300 km");
        assertTrue(distance < 400, "Distance should be less than 400 km");
    }

    @Test
    void roundDistance_WhenTwoDecimalPlaces_ShouldRoundCorrectly() {
        // Given
        double distance = 123.456789;

        // When
        double rounded = DistanceCalculator.roundDistance(distance, 2);

        // Then
        assertEquals(123.46, rounded, 0.001);
    }

    @Test
    void roundDistance_WhenZeroDecimalPlaces_ShouldRoundToInteger() {
        // Given
        double distance = 123.789;

        // When
        double rounded = DistanceCalculator.roundDistance(distance, 0);

        // Then
        assertEquals(124.0, rounded, 0.001);
    }

    @Test
    void roundDistance_WhenThreeDecimalPlaces_ShouldRoundCorrectly() {
        // Given
        double distance = 123.456789;

        // When
        double rounded = DistanceCalculator.roundDistance(distance, 3);

        // Then
        assertEquals(123.457, rounded, 0.0001);
    }

    @Test
    void calculateDistance_WhenNegativeCoordinates_ShouldWorkCorrectly() {
        // Given - Southern Hemisphere locations
        double sydneyLat = -33.8688;
        double sydneyLon = 151.2093;
        double melbourneLat = -37.8136;
        double melbourneLon = 144.9631;

        // When
        double distance = DistanceCalculator.calculateDistance(sydneyLat, sydneyLon, melbourneLat, melbourneLon);

        // Then - Sydney to Melbourne is approximately 713 km
        assertTrue(distance > 650, "Distance should be greater than 650 km");
        assertTrue(distance < 780, "Distance should be less than 780 km");
    }

    @Test
    void calculateDistance_WhenVerySmallDistance_ShouldNotReturnZero() {
        // Given - Two very close points (about 100 meters apart)
        double lat1 = 34.0522;
        double lon1 = -118.2437;
        double lat2 = 34.0530; // Slightly north
        double lon2 = -118.2437;

        // When
        double distance = DistanceCalculator.calculateDistance(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(distance > 0, "Even small distances should be positive");
        assertTrue(distance < 1, "Should be less than 1 km");
    }
}