# Stage 1: Build
FROM --platform=linux/amd64 gradle:8.4.0-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon
COPY . .
RUN gradle build -x test --no-daemon

# Stage 2: Run
FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app

# Install system dependencies for file processing
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    build-essential \
    libgl1-mesa-dri \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Create directories for file uploads
RUN mkdir -p /app/uploads/images \
    && mkdir -p /app/uploads/stl \
    && mkdir -p /tmp/product-images \
    && mkdir -p /tmp/stl-files

# Copy and set up the application
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081

# Use non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring \
    && chown -R spring:spring /app/uploads \
    && chown -R spring:spring /tmp/product-images \
    && chown -R spring:spring /tmp/stl-files

USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"] 