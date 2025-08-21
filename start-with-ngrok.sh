#!/bin/bash

# Product Service with Ngrok Auto-Startup Script
# This script automatically starts ngrok and the Spring Boot service together

echo "🚀 Starting Product Service with Ngrok..."
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}🧹 Cleaning up...${NC}"
    
    # Kill ngrok
    if [ ! -z "$NGROK_PID" ]; then
        echo -e "${YELLOW}🔌 Stopping ngrok (PID: $NGROK_PID)...${NC}"
        kill $NGROK_PID 2>/dev/null || true
    fi
    
    # Kill any remaining ngrok processes
    pkill ngrok 2>/dev/null || true
    
    # Kill Spring Boot app
    if [ ! -z "$SPRING_PID" ]; then
        echo -e "${YELLOW}🛑 Stopping Spring Boot application (PID: $SPRING_PID)...${NC}"
        kill $SPRING_PID 2>/dev/null || true
    fi
    
    # Kill any remaining gradle processes
    pkill -f "gradlew bootRun" 2>/dev/null || true
    
    echo -e "${GREEN}✅ Cleanup complete${NC}"
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check if ngrok is installed
if ! command -v ngrok &> /dev/null; then
    echo -e "${RED}❌ Error: ngrok is not installed or not in PATH${NC}"
    echo -e "${BLUE}💡 Install with: brew install ngrok${NC}"
    exit 1
fi

# Stop any existing ngrok and Spring Boot processes
echo -e "${YELLOW}🔄 Stopping existing processes...${NC}"
pkill ngrok 2>/dev/null || true
pkill -f "gradlew bootRun" 2>/dev/null || true
sleep 2

# Start ngrok in background
echo -e "${BLUE}🌐 Starting ngrok tunnel for port 8081...${NC}"
ngrok http 8081 --log=stdout > ngrok.log 2>&1 &
NGROK_PID=$!

# Wait for ngrok to be ready
echo -e "${YELLOW}⏳ Waiting for ngrok to be ready...${NC}"
sleep 5

# Try to get ngrok URL with retries
NGROK_URL=""
for i in {1..10}; do
    if NGROK_URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | python3 -c "import sys, json; data = json.load(sys.stdin); print(data['tunnels'][0]['public_url'] if data['tunnels'] else '')" 2>/dev/null); then
        if [ ! -z "$NGROK_URL" ]; then
            break
        fi
    fi
    echo -e "${YELLOW}  Attempt $i/10 - waiting for ngrok...${NC}"
    sleep 2
done

# Check if we got the ngrok URL
if [ -z "$NGROK_URL" ]; then
    echo -e "${RED}❌ Failed to get ngrok URL after 10 attempts${NC}"
    echo -e "${BLUE}💡 Check ngrok.log for details${NC}"
    cleanup
    exit 1
fi

echo -e "${GREEN}✅ Ngrok tunnel established!${NC}"
echo -e "${BLUE}🔗 Public URL: ${NGROK_URL}${NC}"
echo -e "${BLUE}🔗 Local URL:  http://localhost:8081${NC}"
echo -e "${BLUE}🔗 Ngrok UI:   http://localhost:4040${NC}"

# Export the ngrok URL for the Spring Boot application
export NGROK_URL="$NGROK_URL"

echo -e "\n${BLUE}🌟 Starting Spring Boot application...${NC}"
echo -e "${YELLOW}   Environment: NGROK_URL=$NGROK_URL${NC}"

# Start Spring Boot application
./gradlew bootRun &
SPRING_PID=$!

echo -e "\n${GREEN}🎉 Services started successfully!${NC}"
echo -e "${BLUE}📋 Service URLs:${NC}"
echo -e "   • Public UI:    ${NGROK_URL}/publish-test.html"
echo -e "   • Local UI:     http://localhost:8081/publish-test.html"
echo -e "   • Health Check: ${NGROK_URL}/actuator/health"
echo -e "   • Ngrok Admin:  http://localhost:4040"
echo -e "\n${YELLOW}💡 Press Ctrl+C to stop all services${NC}"
echo -e "${YELLOW}💡 Images will now be publicly accessible via: ${NGROK_URL}/images/[filename]${NC}"

# Wait for Spring Boot to start
echo -e "\n${YELLOW}⏳ Waiting for Spring Boot to be ready...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Spring Boot application is ready!${NC}"
        break
    fi
    echo -e "${YELLOW}  Attempt $i/30 - waiting for Spring Boot...${NC}"
    sleep 3
done

echo -e "\n${GREEN}🚀 All services are running!${NC}"
echo -e "${BLUE}🎯 You can now upload images and they will be publicly accessible${NC}"

# Keep the script running and wait for Spring Boot process
wait $SPRING_PID
