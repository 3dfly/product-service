# 🚀 Automated Ngrok + Spring Boot Startup Guide

This guide explains how to use the automated startup system that ensures your images are always publicly accessible to Shopify.

## 📁 New Files Created

- `start-with-ngrok.sh` - Automated startup script
- `stop-all.sh` - Clean shutdown script
- `NGROK_STARTUP_GUIDE.md` - This guide

## 🎯 Quick Start

### Start Everything (Recommended)
```bash
./start-with-ngrok.sh
```
This will:
1. ✅ Stop any existing processes
2. 🌐 Start ngrok tunnel on port 8081
3. ⏳ Wait for ngrok to be ready
4. 🔗 Get the public URL automatically
5. 🚀 Start Spring Boot with NGROK_URL environment variable
6. 📊 Display all service URLs

### Stop Everything
```bash
./stop-all.sh
```
This will cleanly stop both ngrok and Spring Boot services.

## 🌟 Benefits

### ✅ **Images Now Work in Shopify**
- **Before**: `http://localhost:8081/images/file.jpg` ❌ (not accessible to Shopify)
- **After**: `https://[random].ngrok-free.app/images/file.jpg` ✅ (publicly accessible)

### 🔄 **Automatic Setup**
- No manual ngrok commands needed
- No manual environment variable setup
- Automatic URL detection and configuration
- Clean startup and shutdown

### 📱 **Multiple Access Points**
When running, you'll have:
- **Public UI**: `https://[ngrok-url]/publish-test.html`
- **Local UI**: `http://localhost:8081/publish-test.html` 
- **Public API**: `https://[ngrok-url]/actuator/health`
- **Ngrok Admin**: `http://localhost:4040`

## 🛠 Alternative Startup Methods

### Local Development Only (No Ngrok)
```bash
./gradlew bootRun
```
Use this when you don't need image uploads to work with Shopify.

### Manual Ngrok + Spring Boot
```bash
# Terminal 1: Start ngrok
ngrok http 8081

# Terminal 2: Get URL and start app
export NGROK_URL=https://[your-ngrok-url]
./gradlew bootRun
```

## 📊 Current Status

### ✅ Working Features
- 🌐 Automatic ngrok tunnel creation
- 🔗 Automatic public URL detection
- 🚀 Spring Boot startup with ngrok integration
- 📸 Images publicly accessible to Shopify
- 🧹 Clean shutdown of all services
- 🔄 Restart capability

### 🎯 Usage Examples

1. **Start development session**:
   ```bash
   ./start-with-ngrok.sh
   ```

2. **Upload and publish products**: 
   - Images will automatically use public URLs
   - Shopify can access all uploaded images

3. **End development session**:
   ```bash
   ./stop-all.sh
   ```

## 🔍 Troubleshooting

### If ngrok fails to start:
1. Check if ngrok is installed: `which ngrok`
2. Check ngrok.log file for errors
3. Ensure port 8081 is not in use: `lsof -i :8081`

### If Spring Boot fails to start:
1. Check if Java is available: `java -version`
2. Ensure Gradle wrapper is executable: `chmod +x gradlew`
3. Check application logs in the terminal

### Check current status:
```bash
# See running processes
ps aux | grep -E "(ngrok|ProductService)" | grep -v grep

# Get current ngrok URL
curl -s http://localhost:4040/api/tunnels | python3 -c "import sys, json; data = json.load(sys.stdin); print(data['tunnels'][0]['public_url'] if data['tunnels'] else 'No tunnels')"

# Test health endpoints
curl http://localhost:8081/actuator/health
curl https://[ngrok-url]/actuator/health
```

## 🎉 Success Indicators

When everything is working correctly, you should see:
- ✅ Green "All services are running!" message
- 🔗 Public and local URLs displayed
- 📸 Images using ngrok URLs in logs like:
  ```
  🔍 NGROK_URL environment variable: https://[random].ngrok-free.app
  🌐 Using ngrok URL: https://[random].ngrok-free.app
  🔗 Final image URL: https://[random].ngrok-free.app/images/[filename]
  ✅ Found valid public images!
  ```

Your Shopify integration now has reliable image access! 🎊
