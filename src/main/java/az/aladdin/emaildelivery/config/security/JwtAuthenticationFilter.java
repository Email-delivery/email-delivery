package az.aladdin.emaildelivery.config.security;

import az.aladdin.emaildelivery.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates each request straight from the JWT claims of the admin panel's own access tokens. Validation is
 * fully stateless: the signature and {@code authorities} (permissions) claim are trusted without a DB lookup.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String token = jwtUtil.resolveToken(request);
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtUtil.isAccessTokenValid(token)) {
                String email = jwtUtil.extractEmail(token);
                List<String> authorities = jwtUtil.extractAuthorities(token);

                AdminPrincipal principal = AdminPrincipal.builder()
                        .userId(jwtUtil.extractUserId(token))
                        .email(email)
                        .firstName(jwtUtil.extractFirstName(token))
                        .lastName(jwtUtil.extractLastName(token))
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities.stream().map(SimpleGrantedAuthority::new).toList()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (principal.getUserId() != null) {
                    MDC.put("userId", String.valueOf(principal.getUserId()));
                }
                log.debug("Authenticated admin user: {}", email);
            } else {
                log.warn("Invalid or expired JWT token presented to admin panel");
            }
        } catch (Exception e) {
            log.warn("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
