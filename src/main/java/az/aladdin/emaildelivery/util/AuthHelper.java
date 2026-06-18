package az.aladdin.emaildelivery.util;

import az.aladdin.emaildelivery.config.security.AdminPrincipal;
import az.aladdin.emaildelivery.exception.ExceptionConstants;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public AdminPrincipal getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new UnauthorizedException(
                    ExceptionConstants.UNAUTHORIZED.getCode(),
                    MessageKeys.EXCEPTION_NO_AUTHENTICATED_USER
            );
        }
        return principal;
    }
}
