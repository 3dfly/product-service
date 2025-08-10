# Address Enrichment Feature

This document demonstrates the new address enrichment feature that automatically geocodes addresses when coordinates are missing.

## Feature Overview

The `findClosestSupplier` API now supports automatic address enrichment:

1. **Coordinates Provided**: Works as before - uses provided latitude/longitude
2. **Coordinates Missing**: Automatically geocodes the `buyerAddress` to get coordinates
3. **Geocoding Fails**: Returns proper error message asking for manual coordinates

## API Changes

### OrderRequest DTO
- `buyerLatitude` and `buyerLongitude` are now **optional** (no longer required)
- `buyerAddress` is still **required**

### Before (Required coordinates):
```json
{
  "materialType": "PLA",
  "color": "Red",
  "requiredQuantityKg": 5.0,
  "buyerAddress": "New York, NY",
  "buyerLatitude": 40.7128,
  "buyerLongitude": -74.0060
}
```

### After (Coordinates optional):
```json
{
  "materialType": "PLA",
  "color": "Red", 
  "requiredQuantityKg": 5.0,
  "buyerAddress": "New York, NY"
}
```

## Implementation Details

### New Components Added:

1. **GeocodingService**: Converts addresses to coordinates using OpenStreetMap Nominatim API
2. **GeocodingResponse**: DTO for geocoding results
3. **WebClientConfig**: Configuration for HTTP client
4. **Address Enrichment Logic**: In OrderService.enrichCoordinatesIfNeeded()

### Configuration Properties:
```properties
# Enable/disable geocoding service
geocoding.enabled=true
# Timeout for geocoding requests (ms)
geocoding.timeout=5000
```

## Testing the Feature

### Test Case 1: Address with Missing Coordinates
```bash
curl -X POST http://localhost:8081/orders/find-closest-supplier \
  -H "Content-Type: application/json" \
  -d '{
    "materialType": "PLA",
    "color": "Red",
    "requiredQuantityKg": 5.0,
    "buyerAddress": "1600 Amphitheatre Parkway, Mountain View, CA"
  }'
```

**Expected**: Should geocode the address to coordinates and find the closest supplier.

### Test Case 2: Address with Coordinates (No Change)
```bash
curl -X POST http://localhost:8081/orders/find-closest-supplier \
  -H "Content-Type: application/json" \
  -d '{
    "materialType": "PLA", 
    "color": "Red",
    "requiredQuantityKg": 5.0,
    "buyerAddress": "1600 Amphitheatre Parkway, Mountain View, CA",
    "buyerLatitude": 37.4221,
    "buyerLongitude": -122.0841
  }'
```

**Expected**: Should use provided coordinates directly (no geocoding call).

### Test Case 3: Invalid Address
```bash
curl -X POST http://localhost:8081/orders/find-closest-supplier \
  -H "Content-Type: application/json" \
  -d '{
    "materialType": "PLA",
    "color": "Red", 
    "requiredQuantityKg": 5.0,
    "buyerAddress": "Invalid Address 12345 XYZ"
  }'
```

**Expected**: Should return 400 Bad Request with geocoding error message.

## Use Cases

This feature is especially useful for integrations with:
- **Etsy**: Provides buyer addresses but not coordinates
- **Shopify**: Provides addresses but coordinates may not be available  
- **Other E-commerce platforms**: Most provide addresses, fewer provide coordinates

## Error Handling

The service gracefully handles various error scenarios:
- **Network timeouts**: Returns error after 5 seconds
- **Invalid addresses**: Returns meaningful error messages
- **Service unavailable**: Falls back to manual coordinate requirement
- **Geocoding disabled**: Can be turned off via configuration

## Performance Considerations

- **Caching**: Consider adding Redis cache for frequently geocoded addresses
- **Rate Limiting**: OpenStreetMap Nominatim has usage limits
- **Timeouts**: Configured to 5 seconds to avoid long waits
- **Fallback**: Manual coordinates always work as backup
