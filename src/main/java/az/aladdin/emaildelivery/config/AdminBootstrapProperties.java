package az.aladdin.emaildelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Initial super-admin provisioned on first startup (see {@code application.yaml} under {@code admin.bootstrap}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "admin.bootstrap")
public class AdminBootstrapProperties {

    private boolean enabled = true;
    private String email = "aladdin19.11@gmail.com";
    private String password = "Aladdin12";
    private String firstName = "Aladdin";
    private String lastName = "Biyabangerd";
}
