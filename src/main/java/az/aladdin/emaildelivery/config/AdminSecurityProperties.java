package az.aladdin.emaildelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunables for the admin panel's own access tokens (see {@code application.yaml} under {@code admin.security}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "admin.security")
public class AdminSecurityProperties {

    private long accessTokenTtlMinutes = 480;
    private long refreshTokenValidityDays = 7;
    private long passwordResetExpirationMinutes = 15;
}
