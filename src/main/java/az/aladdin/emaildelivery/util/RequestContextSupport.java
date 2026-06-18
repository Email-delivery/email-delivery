package az.aladdin.emaildelivery.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads selected headers from the inbound request so they can be forwarded to the managed back-ends — most
 * importantly the {@code Accept-Language} header used for localization.
 */
public final class RequestContextSupport {

    private RequestContextSupport() {
    }

    public static String currentAuthorizationHeader() {
        return header(HttpHeaders.AUTHORIZATION);
    }

    public static String currentAcceptLanguage() {
        return header(HttpHeaders.ACCEPT_LANGUAGE);
    }

    private static String header(String name) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String value = request.getHeader(name);
        return (value != null && !value.isBlank()) ? value : null;
    }
}
