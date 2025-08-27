package com.threedfly.productservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
		scanBasePackages = {
				"com.threedfly.productservice",
				"com.threedfly.shopify",      // Shopify integration module
				"service",                    // Infrastructure services  
				"repository",                 // Infrastructure repositories
				"mapper",                     // Infrastructure mappers
				"dto",                        // Infrastructure DTOs
				"exception",                  // Infrastructure exceptions
				"configuration"				  // Common
		}
)
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "entity")
public class ProductServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProductServiceApplication.class);

	public static void main(String[] args) {
		logger.info("🚀 Starting Product Service Application...");
		SpringApplication.run(ProductServiceApplication.class, args);
		logger.info("✅ Product Service Application started successfully!");
	}

} 