#!/bin/bash

# Product Service Stop Script
# Cleanly stops both ngrok and Spring Boot service

echo "🛑 Stopping Product Service and Ngrok..."
echo "======================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Stop Spring Boot (gradle)
echo -e "${YELLOW}🔄 Stopping Spring Boot application...${NC}"
pkill -f "gradlew bootRun" 2>/dev/null && echo -e "${GREEN}✅ Spring Boot stopped${NC}" || echo -e "${YELLOW}⚠️ No Spring Boot process found${NC}"

# Stop ngrok
echo -e "${YELLOW}🔄 Stopping ngrok...${NC}"
pkill ngrok 2>/dev/null && echo -e "${GREEN}✅ Ngrok stopped${NC}" || echo -e "${YELLOW}⚠️ No ngrok process found${NC}"

# Clean up any remaining Java processes related to our app
echo -e "${YELLOW}🧹 Cleaning up remaining processes...${NC}"
pkill -f "ProductServiceApplication" 2>/dev/null || true

# Remove log files if they exist
if [ -f "ngrok.log" ]; then
    echo -e "${YELLOW}🗑️ Cleaning up ngrok.log${NC}"
    rm -f ngrok.log
fi

echo -e "\n${GREEN}✅ All services stopped successfully!${NC}"
echo -e "${BLUE}💡 Use ./start-with-ngrok.sh to restart with ngrok${NC}"
echo -e "${BLUE}💡 Use ./gradlew bootRun to start without ngrok${NC}"
