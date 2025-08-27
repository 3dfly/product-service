package configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(25))
                .writeTimeout(Duration.ofSeconds(25))
                // .retryOnConnectionFailure(true) // optional
                .build();
    }
}
