package az.aladdin.emaildelivery.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static az.aladdin.emaildelivery.exception.MessageKeys.EXCEPTION_JWT_SECRET_TOO_SHORT;

/**
 * JWT helper used both to mint the admin panel's own access tokens (local authentication) and to read tokens.
 * Tokens are signed with the shared secret so the same key also lets us mint PMS/RMS-compatible service tokens.
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    @Value("${security.jwtProperties.secret}")
    private String secretKey;

    private Key key;

    private Key signingKey() {
        if (key != null) {
            return key;
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 64) {
            throw new IllegalArgumentException(EXCEPTION_JWT_SECRET_TOO_SHORT);
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        return key;
    }

    /**
     * Mints a signed access token. {@code claims} are written first so reserved claims (subject, dates) always win.
     */
    public String generateToken(String subject, Map<String, Object> claims, Duration ttl) {
        Instant now = Instant.now();
        Map<String, Object> payload = claims == null ? new HashMap<>() : new HashMap<>(claims);
        payload.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        return Jwts.builder()
                .setClaims(payload)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * A token is valid when its signature checks out, it is not expired, and it is an access token
     * (2FA temp tokens must never authenticate API calls).
     */
    public boolean isAccessTokenValid(String token) {
        try {
            String tokenType = extractTokenType(token);
            if (tokenType != null && !ACCESS_TOKEN_TYPE.equals(tokenType)) {
                log.debug("Token rejected: token_type is not access ({})", tokenType);
                return false;
            }
            return !isTokenExpired(token) && extractEmail(token) != null;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.debug("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        try {
            Object authorities = extractAllClaims(token).get("authorities");
            if (authorities instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.debug("Error extracting authorities from token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Long extractUserId(String token) {
        return extractNumericClaim(token, "user_id");
    }

    public String extractFirstName(String token) {
        return extractStringClaim(token, "first_name");
    }

    public String extractLastName(String token) {
        return extractStringClaim(token, "last_name");
    }

    public Long extractHotelId(String token) {
        return extractNumericClaim(token, "hotel_id");
    }

    public String extractTokenType(String token) {
        try {
            Object value = extractAllClaims(token).get(TOKEN_TYPE_CLAIM);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractNumericClaim(String token, String claimName) {
        try {
            Object value = extractAllClaims(token).get(claimName);
            if (value instanceof Number number) {
                return number.longValue();
            }
            return null;
        } catch (Exception e) {
            log.debug("Error extracting {} from token: {}", claimName, e.getMessage());
            return null;
        }
    }

    private String extractStringClaim(String token, String claimName) {
        try {
            Object value = extractAllClaims(token).get(claimName);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
