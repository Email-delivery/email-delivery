package az.aladdin.emaildelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "admin.cors")
public class AdminCorsProperties {

    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*"));

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    private List<String> allowedHeaders = List.of("*");

    private List<String> exposedHeaders = List.of("Authorization", "Link", "X-Total-Count");

    private boolean allowCredentials = true;

    private long maxAgeSeconds = 3600L;
}
