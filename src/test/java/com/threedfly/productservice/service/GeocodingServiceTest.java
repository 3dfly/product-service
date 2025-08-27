package com.threedfly.productservice.service;

import dto.GeocodingResponse;
import service.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geocodingService, "timeoutMs", 5000L);
        ReflectionTestUtils.setField(geocodingService, "geocodingEnabled", true);
    }

    @Test
    void areCoordinatesMissing_WhenBothNull_ShouldReturnTrue() {
        // When & Then
        assertTrue(geocodingService.areCoordinatesMissing(null, null));
    }

    @Test
    void areCoordinatesMissing_WhenOnlyLatitudeNull_ShouldReturnFalse() {
        // When & Then
        assertFalse(geocodingService.areCoordinatesMissing(null, -118.2437));
    }

    @Test
    void areCoordinatesMissing_WhenOnlyLongitudeNull_ShouldReturnFalse() {
        // When & Then
        assertFalse(geocodingService.areCoordinatesMissing(34.0522, null));
    }

    @Test
    void areCoordinatesMissing_WhenBothProvided_ShouldReturnFalse() {
        // When & Then
        assertFalse(geocodingService.areCoordinatesMissing(34.0522, -118.2437));
    }

    @Test
    void geocodeAddress_WhenNullAddress_ShouldReturnFailure() {
        // When
        GeocodingResponse result = geocodingService.geocodeAddress(null);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Address is null or empty", result.getErrorMessage());
    }

    @Test
    void geocodeAddress_WhenEmptyAddress_ShouldReturnFailure() {
        // When
        GeocodingResponse result = geocodingService.geocodeAddress("   ");

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Address is null or empty", result.getErrorMessage());
    }

    @Test
    void geocodeAddress_WhenGeocodingDisabled_ShouldReturnFailure() {
        // Given
        ReflectionTestUtils.setField(geocodingService, "geocodingEnabled", false);

        // When
        GeocodingResponse result = geocodingService.geocodeAddress("New York, NY");

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Geocoding service is disabled", result.getErrorMessage());
    }

    // Note: Integration tests for actual geocoding would require external API calls
    // For unit tests, we focus on the business logic and error handling
}
