# Product Service API - Postman Testing Guide

## 🚀 Quick Start

### 1. Import the Collection & Environment

1. **Import Collection**: Import `Product-Service-API.postman_collection.json` into Postman
2. **Import Environment**: Import `Product-Service-Environment.postman_environment.json` into Postman
3. **Select Environment**: Choose "Product Service - Local" environment in the top-right dropdown

### 2. Start the Application

```bash
# Start the Spring Boot application
./gradlew bootRun

# Or alternatively:
java -jar build/libs/product-service-*.jar
```

The application will start on `http://localhost:8080`

### 3. Verify Connection

Run the **Health Check** request first to ensure the service is running.

---

## 📁 Collection Structure

### **Health Check**
- `GET /health` - Service health check

### **Products** (5 endpoints)
- `GET /products` - Get all products
- `GET /products/{id}` - Get product by ID
- `POST /products` - Create new product
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product

### **Suppliers** (13 endpoints)
- Basic CRUD operations
- Search by user ID, email, city, state, country
- Geographic search within radius
- Supplier verification and activation/deactivation

### **Shops** (12 endpoints)
- Basic CRUD operations
- Search by seller ID, name, description, address
- Shop management and product counting

### **Filament Stock** (16 endpoints)
- Complete inventory management
- Search by supplier, material type, color
- Stock reservation and release system
- Low stock and expiry monitoring

### **Validation Test Cases** (4 endpoints)
- Test API validation for various error scenarios
- Demonstrates proper error handling

---

## 🧪 Testing Workflow

### **Recommended Testing Order:**

1. **Health Check** - Verify service is running
2. **Create Supplier** - Create a test supplier first
3. **Create Shop** - Create a shop for the supplier
4. **Create Filament Stock** - Add inventory for the supplier
5. **Create Product** - Create products for the shop
6. **Test Search & Filter Operations**
7. **Test Validation Scenarios**

---

## 💡 Key Features to Test

### **API-Level Validation**
- Try creating entities with missing required fields
- Test with invalid data formats (negative numbers, invalid emails)
- Verify proper 400 Bad Request responses

### **orElseThrow() Pattern**
- Try accessing non-existent IDs (e.g., `/products/999999`)
- Verify you get RuntimeException with descriptive messages
- No more `Optional.empty()` responses

### **Complete DTO Architecture**
- All responses use Response DTOs (ProductResponse, SupplierResponse, etc.)
- All request bodies use Request DTOs (ProductRequest, SupplierRequest, etc.)
- No direct entity exposure

### **Advanced Features**

#### **Filament Stock Management:**
```
1. Create a supplier
2. Add filament stock for that supplier
3. Reserve some stock: POST /filament-stock/{id}/reserve?quantityKg=5.0
4. Check available quantity has decreased
5. Release stock: POST /filament-stock/{id}/release?quantityKg=2.0
```

#### **Geographic Search:**
```
1. Create suppliers with latitude/longitude
2. Use: GET /suppliers/nearby?latitude=34.0522&longitude=-118.2437&radiusKm=50
3. Test different radius values
```

#### **Inventory Queries:**
```
1. GET /filament-stock/low-stock?threshold=10.0
2. GET /filament-stock/sufficient-quantity?requiredKg=25.0
3. GET /filament-stock/search?materialType=PLA&color=Red
```

---

## 🔧 Environment Variables

The environment includes these variables for easy testing:

- `baseUrl` - http://localhost:8080
- `testSupplierId` - 1
- `testShopId` - 1  
- `testProductId` - 1
- `testFilamentStockId` - 1

You can modify these after creating your test data.

---

## 📊 Expected Response Formats

### **Product Response:**
```json
{
  "id": 1,
  "name": "3D Printed Phone Case",
  "description": "Custom phone case made with PLA filament",
  "price": 25.99,
  "imageUrl": "https://example.com/phone-case.jpg",
  "stlFileUrl": "https://example.com/phone-case.stl", 
  "sellerId": 1,
  "shopId": 1
}
```

### **Validation Error Response:**
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/products"
}
```

### **Not Found Error Response:**
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error", 
  "message": "Product not found with ID: 999",
  "path": "/products/999"
}
```

---

## 🎯 Test Coverage Goals

- ✅ **API Validation**: All validation annotations working
- ✅ **Error Handling**: Proper HTTP status codes
- ✅ **CRUD Operations**: All entities fully functional
- ✅ **Search & Filter**: Advanced query capabilities
- ✅ **Business Logic**: Stock management, reservations
- ✅ **Geographic Features**: Location-based searches

---

## 🐛 Troubleshooting

### **Common Issues:**

1. **Connection Refused**
   - Ensure application is running on port 8080
   - Check application logs for startup errors

2. **404 Not Found**
   - Verify endpoint URLs match the collection
   - Check if you have the correct base URL

3. **400 Bad Request** 
   - This is expected for validation test cases
   - Check request body format for other endpoints

4. **500 Internal Server Error**
   - Usually means entity not found (orElseThrow pattern)
   - Create test data first before referencing IDs

### **Application Logs:**
```bash
# View application logs
./gradlew bootRun

# Or if running as JAR:
java -jar build/libs/product-service-*.jar
```

---

**Happy Testing! 🧪✨**