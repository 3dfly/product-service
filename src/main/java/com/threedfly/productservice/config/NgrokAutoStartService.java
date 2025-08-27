package com.threedfly.productservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Automatically starts ngrok tunnel when the application starts.
 * This works regardless of how the application is started (IntelliJ, command line, etc.)
 */
@Component
public class NgrokAutoStartService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NgrokAutoStartService.class);
    
    @Value("${ngrok.auto-start.enabled:true}")
    private boolean ngrokAutoStartEnabled;
    
    @Value("${ngrok.port:${server.port:8081}}")
    private int ngrokPort;
    
    @Value("${ngrok.timeout:30}")
    private int ngrokTimeoutSeconds;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private Process ngrokProcess;
    private String ngrokUrl;

    public NgrokAutoStartService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        if (!ngrokAutoStartEnabled) {
            logger.info("🔌 Ngrok auto-start is disabled");
            return;
        }

        // Check if NGROK_URL is already set (e.g., from script)
        String existingNgrokUrl = System.getenv("NGROK_URL");
        if (existingNgrokUrl != null && !existingNgrokUrl.isEmpty()) {
            logger.info("🔗 Using existing NGROK_URL from environment: {}", existingNgrokUrl);
            return;
        }

        logger.info("🚀 Auto-starting ngrok tunnel for port {}...", ngrokPort);
        
        try {
            startNgrok();
            Thread.sleep(3000); // Give ngrok time to start
            
            String url = getNgrokUrl();
            if (url != null) {
                ngrokUrl = url;
                
                // Set as system property for the application to use
                System.setProperty("NGROK_URL", ngrokUrl);
                
                logger.info("✅ Ngrok tunnel established!");
                logger.info("🔗 Public URL: {}", ngrokUrl);
                logger.info("🔗 Local URL:  http://localhost:{}", ngrokPort);
                logger.info("🔗 Ngrok UI:   http://localhost:4040");
                logger.info("📸 Images will now be publicly accessible via: {}/images/[filename]", ngrokUrl);
                
            } else {
                logger.error("❌ Failed to get ngrok URL");
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to start ngrok: {}", e.getMessage(), e);
            logger.info("💡 Application will continue without ngrok. Images will use localhost URLs.");
        }
    }

    private void startNgrok() throws IOException {
        // Check if ngrok is already running
        if (isNgrokRunning()) {
            logger.info("🔄 Ngrok is already running, using existing tunnel");
            return;
        }

        // Kill any existing ngrok processes
        try {
            ProcessBuilder killBuilder = new ProcessBuilder("pkill", "ngrok");
            killBuilder.start().waitFor(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore - process might not exist
        }

        // Start new ngrok process
        ProcessBuilder builder = new ProcessBuilder("ngrok", "http", String.valueOf(ngrokPort));
        builder.redirectErrorStream(true);
        
        ngrokProcess = builder.start();
        logger.info("🌐 Started ngrok process for port {}", ngrokPort);
    }

    private boolean isNgrokRunning() {
        try {
            Request request = new Request.Builder()
                    .url("http://localhost:4040/api/tunnels")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String getNgrokUrl() {
        for (int i = 0; i < ngrokTimeoutSeconds; i++) {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:4040/api/tunnels")
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JsonNode json = objectMapper.readTree(responseBody);
                        
                        JsonNode tunnels = json.get("tunnels");
                        if (tunnels != null && tunnels.isArray() && tunnels.size() > 0) {
                            JsonNode tunnel = tunnels.get(0);
                            String publicUrl = tunnel.get("public_url").asText();
                            
                            // Prefer https URL if available
                            for (JsonNode t : tunnels) {
                                String url = t.get("public_url").asText();
                                if (url.startsWith("https://")) {
                                    return url;
                                }
                            }
                            return publicUrl;
                        }
                    }
                }
                
                Thread.sleep(1000);
                logger.debug("⏳ Waiting for ngrok tunnel... ({}/{})", i + 1, ngrokTimeoutSeconds);
                
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    @EventListener
    public void onApplicationShutdown(ContextClosedEvent event) {
        if (ngrokProcess != null && ngrokProcess.isAlive()) {
            logger.info("🛑 Stopping ngrok process...");
            ngrokProcess.destroy();
            try {
                if (!ngrokProcess.waitFor(5, TimeUnit.SECONDS)) {
                    logger.warn("⚠️ Ngrok process didn't stop gracefully, forcing termination");
                    ngrokProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ngrokProcess.destroyForcibly();
            }
            logger.info("✅ Ngrok process stopped");
        }
    }

    public String getCurrentNgrokUrl() {
        return ngrokUrl;
    }

    public boolean isNgrokActive() {
        return ngrokUrl != null && !ngrokUrl.isEmpty();
    }
}
