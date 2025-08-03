#!/bin/bash

echo "🚀 Starting Product Service for Postman Testing..."
echo "========================================"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found. Make sure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📦 Building application..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo "✅ Build successful!"
echo ""
echo "🌟 Starting application on http://localhost:8080"
echo "📍 Health check will be available at: http://localhost:8080/health"
echo ""
echo "📚 Import these files into Postman:"
echo "   - Product-Service-API.postman_collection.json"
echo "   - Product-Service-Environment.postman_environment.json"
echo ""
echo "📖 See POSTMAN_TESTING_GUIDE.md for detailed instructions"
echo ""
echo "⏹️  Press Ctrl+C to stop the application"
echo "========================================"
echo ""

# Start the application
./gradlew bootRun