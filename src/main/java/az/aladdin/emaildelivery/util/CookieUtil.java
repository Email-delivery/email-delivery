package az.aladdin.emaildelivery.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/admin/v1/auth";

    @NonFinal
    @Value("${security.cookie.secure:false}")
    boolean secureCookie;

    @NonFinal
    @Value("${security.cookie.same-site:Lax}")
    String sameSite;

    @NonFinal
    @Value("${admin.security.refresh-token-validity-days:7}")
    long refreshTokenValidityInDays;

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        long maxAgeSeconds = refreshTokenValidityInDays * 24 * 60 * 60;
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
