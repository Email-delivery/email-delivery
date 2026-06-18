package az.aladdin.emaildelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables {@code @CreatedDate}/{@code @LastModifiedDate} population on persistent entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
