package az.aladdin.emaildelivery.config.security;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * The authenticated admin-panel user, derived from the locally-issued JWT and stored as the {@code principal}
 * in the Spring Security context for the duration of a request.
 */
@Getter
@Builder
public class AdminPrincipal {

    private final Long userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final List<String> authorities;
}
