package com.threedfly.productservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("🏥 Health check endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "product-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        log.info("🚦 Readiness check endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "product-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Service is ready to accept requests");
        
        return ResponseEntity.ok(response);
    }
} 