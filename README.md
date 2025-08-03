# Product Service

A comprehensive Spring Boot microservice for product catalog management with advanced features including category hierarchies, supplier management, and 3D printing integration.

## 🚀 Features

### Core Product Management
- **CRUD Operations**: Create, read, update, delete products
- **SKU Management**: Unique stock keeping unit tracking
- **Status Tracking**: DRAFT → ACTIVE → OUT_OF_STOCK → DISCONTINUED → ARCHIVED
- **Stock Management**: Real-time inventory tracking
- **Search & Filtering**: Advanced product search capabilities

### Category Management
- **Hierarchical Categories**: Support for parent-child category relationships
- **URL-Friendly Slugs**: SEO-optimized category URLs
- **Active/Inactive States**: Category lifecycle management
- **Sort Ordering**: Customizable category display order

### Supplier Management
- **Supplier Onboarding**: Complete supplier registration process
- **Verification System**: Supplier verification workflow
- **Rating System**: Supplier performance tracking (0.0 - 5.0)
- **Status Management**: PENDING → ACTIVE → SUSPENDED → BLOCKED → INACTIVE
- **Multi-location Support**: Global supplier network

### 3D Printing Integration
- **STL File Support**: Native 3D model file handling
- **3D Printable Products**: Specialized product type for 3D printing
- **Weight & Dimensions**: Physical product specifications
- **Compatible with Order Service**: Seamless integration for 3D printing orders

### Advanced Features
- **Tag System**: Product tagging for enhanced categorization
- **Price Range Filtering**: Search products by price range
- **Image Management**: Product image URL support
- **Multi-file Support**: Support for multiple file types

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend/     │    │ Product Service │    │   Order Service │
│   E-commerce    │◄──►│  (Spring Boot)  │◄──►│                 │
│   Platform      │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   Database      │
                       │   (H2/MySQL)    │
                       └─────────────────┘
```

## 📊 API Endpoints

### Product API (`/products`)

#### Create Product
```http
POST /products
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop with RTX graphics",
  "sku": "LAPTOP-001",
  "price": 1299.99,
  "stockQuantity": 50,
  "weight": 2500.0,
  "dimensions": "35x25x2 cm",
  "categoryId": 1,
  "supplierId": 1,
  "imageUrl": "https://example.com/laptop.jpg",
  "is3DPrintable": false,
  "tags": ["gaming", "laptop", "electronics"]
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop with RTX graphics",
  "sku": "LAPTOP-001",
  "price": 1299.99,
  "stockQuantity": 50,
  "weight": 2500.0,
  "dimensions": "35x25x2 cm",
  "status": "DRAFT",
  "category": {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics"
  },
  "supplier": {
    "id": 1,
    "companyName": "Tech Distributors Inc",
    "verified": true
  },
  "imageUrl": "https://example.com/laptop.jpg",
  "is3DPrintable": false,
  "tags": ["gaming", "laptop", "electronics"],
  "createdAt": "2025-01-31T10:00:00",
  "updatedAt": "2025-01-31T10:00:00"
}
```

#### 3D Printable Product Example
```http
POST /products
Content-Type: application/json

{
  "name": "Custom Phone Case",
  "description": "3D printable phone case with custom design",
  "sku": "3D-CASE-001",
  "price": 15.99,
  "stockQuantity": 0,
  "weight": 25.0,
  "dimensions": "15x8x1 cm",
  "categoryId": 2,
  "supplierId": 3,
  "stlFileUrl": "https://example.com/models/phone-case.stl",
  "is3DPrintable": true,
  "tags": ["3d-printing", "phone-case", "custom"]
}
```

### Category API (`/categories`)

#### Create Category
```http
POST /categories
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "slug": "electronics",
  "parentCategoryId": null,
  "sortOrder": 1,
  "iconUrl": "https://example.com/icons/electronics.svg"
}
```

#### Create Subcategory
```http
POST /categories
Content-Type: application/json

{
  "name": "Laptops",
  "description": "Laptop computers and accessories",
  "slug": "laptops",
  "parentCategoryId": 1,
  "sortOrder": 1
}
```

### Supplier API (`/suppliers`)

#### Register Supplier
```http
POST /suppliers
Content-Type: application/json

{
  "companyName": "Tech Distributors Inc",
  "contactPerson": "John Smith",
  "email": "john@techdist.com",
  "phone": "+1-555-123-4567",
  "address": "123 Tech Street",
  "city": "San Francisco",
  "state": "CA",
  "country": "USA",
  "postalCode": "94105",
  "website": "https://techdist.com",
  "taxId": "12-3456789",
  "notes": "Specialized in electronics distribution"
}
```

## 🔍 Advanced Search & Filtering

### Search Products
```bash
# Search by keyword
GET /products/search?keyword=laptop

# Filter by category
GET /products/category/1

# Filter by supplier
GET /products/supplier/2

# Filter by status
GET /products/status/ACTIVE

# Filter by price range
GET /products/price-range?minPrice=100&maxPrice=500

# Filter by tags
GET /products/tags?tags=gaming,electronics

# Get 3D printable products
GET /products/3d-printable

# Get products in stock
GET /products/in-stock
```

### Category Navigation
```bash
# Get all categories
GET /categories

# Get active categories only
GET /categories/active

# Get top-level categories
GET /categories/top-level

# Get subcategories
GET /categories/1/subcategories

# Get category by slug
GET /categories/slug/electronics
```

### Supplier Management
```bash
# Get all suppliers
GET /suppliers

# Get verified suppliers
GET /suppliers/verified

# Get active verified suppliers
GET /suppliers/active-verified

# Search suppliers
GET /suppliers/search?keyword=tech

# Verify supplier
PATCH /suppliers/1/verify

# Update supplier status
PATCH /suppliers/1/status?status=ACTIVE
```

## 🔧 Configuration

### Application Properties
```properties
# Product Service Configuration
product.default.currency=USD
product.stock.warning.threshold=10
product.sku.prefix=PRD

# Category Configuration
category.slug.max.length=100
category.name.max.length=255

# Supplier Configuration
supplier.verification.required=true
supplier.rating.max=5.0
supplier.rating.min=0.0

# File Upload Configuration
product.image.upload.path=/tmp/product-images
product.image.max.size=5MB
product.3d.stl.upload.path=/tmp/stl-files
product.3d.stl.max.size=50MB

# Search Configuration
product.search.max.results=100
product.search.min.keyword.length=2
```

### Environment Variables (Production)
```bash
# Database Configuration
DB_HOST=your-mysql-host
DB_PORT=3306
DB_NAME=product_service_db
DB_USERNAME=your-username
DB_PASSWORD=your-password

# File Storage
IMAGE_UPLOAD_PATH=/app/uploads/images
STL_UPLOAD_PATH=/app/uploads/stl

# Server Configuration
SERVER_PORT=8081
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Test Product API
```bash
# Test product creation
curl -X POST "http://localhost:8081/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-001",
    "price": 99.99,
    "stockQuantity": 10,
    "categoryId": 1,
    "supplierId": 1
  }'

# Test product search
curl "http://localhost:8081/products/search?keyword=test"

# Test 3D printable products
curl "http://localhost:8081/products/3d-printable"
```

## 🚀 Deployment

### Local Development
```bash
./gradlew bootRun
```

The service will be available at: `http://localhost:8081`

### AWS Deployment
```bash
# Deploy to AWS ECS with load balancer
./setup-aws.sh
```

### Docker Deployment
```bash
# Build image
docker build -t product-service .

# Run container
docker run -p 8081:8081 product-service
```

## 📈 Monitoring

### Health Checks
- **Application Health**: `GET /health`
- **Readiness Check**: `GET /health/ready`
- **Database Status**: Included in health response

### Key Metrics
- **Product Count**: Monitor total products by status
- **Category Usage**: Track products per category
- **Supplier Performance**: Monitor supplier ratings and verification status
- **Search Performance**: Track popular search terms

## 🔒 Security

### Input Validation
- **Comprehensive Validation**: All input fields validated with appropriate constraints
- **SQL Injection Protection**: JPA/Hibernate protection
- **XSS Protection**: Input sanitization

### Data Protection
- **Unique Constraints**: SKU, email, company name uniqueness
- **Status Validation**: Proper status transition management
- **File Upload Security**: File type and size restrictions

## 🎯 Key Benefits

1. **Comprehensive Product Management**: Full product lifecycle support from draft to archive
2. **Scalable Architecture**: Spring Boot microservice design ready for enterprise use
3. **3D Printing Ready**: Native support for STL files and 3D printable products
4. **Advanced Search**: Multiple search and filtering options for enhanced user experience
5. **Supplier Network**: Complete supplier management with verification system
6. **Category Hierarchy**: Flexible category structure for complex product catalogs
7. **Production Ready**: Comprehensive error handling, monitoring, and AWS deployment

## 🔗 Integration

### Order Service Integration
This product service is designed to work seamlessly with the order-service:

- **Product References**: Orders reference products by ID
- **Stock Updates**: Real-time stock management during order processing
- **3D Printing**: Special handling for 3D printable products
- **Supplier Coordination**: Supplier information for order fulfillment

### Frontend Integration
```javascript
// Example frontend integration
const productService = {
  async getProducts() {
    return fetch('/products').then(res => res.json());
  },
  
  async searchProducts(keyword) {
    return fetch(`/products/search?keyword=${keyword}`).then(res => res.json());
  },
  
  async getCategories() {
    return fetch('/categories/active').then(res => res.json());
  }
};
```

## 📞 Support

For technical support or questions:
- Check application logs for detailed error information
- Use health endpoints for system status verification
- Monitor database connection status
- Review AWS CloudWatch logs for production issues

---

**Ready for Production**: This implementation includes all necessary components for a production-ready product catalog management system with advanced features for modern e-commerce platforms. 