package az.aladdin.emaildelivery.service.auth;

import az.aladdin.emaildelivery.config.AdminSecurityProperties;
import az.aladdin.emaildelivery.config.security.AdminPrincipal;
import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.ExpiredRefreshTokenException;
import az.aladdin.emaildelivery.exception.ForbiddenException;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.exception.UnauthorizedException;
import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.entity.PasswordResetTokenEntity;
import az.aladdin.emaildelivery.model.entity.RefreshTokenEntity;
import az.aladdin.emaildelivery.model.enums.EntityStatus;
import az.aladdin.emaildelivery.model.enums.OtpPurpose;
import az.aladdin.emaildelivery.model.request.auth.AdminLoginRequest;
import az.aladdin.emaildelivery.model.request.auth.OtpRequest;
import az.aladdin.emaildelivery.model.request.auth.RegisterRequest;
import az.aladdin.emaildelivery.model.request.auth.ResendOtpRequest;
import az.aladdin.emaildelivery.model.request.auth.ResetPasswordRequest;
import az.aladdin.emaildelivery.model.request.auth.VerifyCodeRequest;
import az.aladdin.emaildelivery.model.response.auth.AdminAuthResponse;
import az.aladdin.emaildelivery.model.response.auth.AdminLoginResponse;
import az.aladdin.emaildelivery.model.response.auth.CurrentAdminResponse;
import az.aladdin.emaildelivery.model.response.auth.VerifyCodeResponse;
import az.aladdin.emaildelivery.repository.AdminUserRepository;
import az.aladdin.emaildelivery.repository.PasswordResetTokenRepository;
import az.aladdin.emaildelivery.repository.RefreshTokenRepository;
import az.aladdin.emaildelivery.util.AuthHelper;
import az.aladdin.emaildelivery.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_REQUIRES_ACTIVATION = "REQUIRES_ACTIVATION";
    private static final int MAX_FAILED_PASSWORD_RESET_ATTEMPTS = 5;
    private static final long PASSWORD_RESET_LOCK_DURATION_MINUTES = 15;

    private final AdminUserRepository adminUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdminSecurityProperties securityProperties;
    private final AuthHelper authHelper;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    private final Map<String, PasswordResetAttemptState> passwordResetAttemptState = new ConcurrentHashMap<>();

    @Transactional
    public void register(RegisterRequest request) {
        validatePasswordMatch(request.getPassword(), request.getPasswordConfirm());
        String email = normalizeEmail(request.getEmail());

        AdminUser user = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ForbiddenException(EntityNames.REGISTRATION_NOT_INVITED,
                        MessageKeys.EXCEPTION_REGISTRATION_NOT_INVITED));

        if (user.getStatus() == EntityStatus.ACTIVE) {
            throw new BadException(EntityNames.EMAIL_ALREADY_EXISTS, MessageKeys.EXCEPTION_EMAIL_ALREADY_EXISTS, email);
        }
        if (user.getStatus() == EntityStatus.INACTIVE) {
            throw new ForbiddenException(EntityNames.ACCOUNT_INACTIVE, MessageKeys.EXCEPTION_ACCOUNT_INACTIVE);
        }
        if (user.getStatus() != EntityStatus.PENDING) {
            throw new ForbiddenException(EntityNames.REGISTRATION_NOT_INVITED,
                    MessageKeys.EXCEPTION_REGISTRATION_NOT_INVITED);
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(EntityStatus.PENDING);
        adminUserRepository.save(user);

        otpService.sendOtp(user.getEmail(), user.displayName(), OtpPurpose.ACCOUNT_ACTIVATION);
        log.info("Admin registration completed for '{}', activation OTP sent", email);
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AdminUser user;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
            user = ((az.aladdin.emaildelivery.config.security.AdminUserDetails) authentication.getPrincipal())
                    .getAdminUser();
        } catch (AuthenticationException ex) {
            log.warn("Admin login failed for '{}': {}", email, ex.getMessage());
            throw new UnauthorizedException(EntityNames.INVALID_CREDENTIALS, MessageKeys.EXCEPTION_INVALID_CREDENTIALS);
        }

        if (user.getStatus() == EntityStatus.INACTIVE) {
            throw new ForbiddenException(EntityNames.ACCOUNT_INACTIVE, MessageKeys.EXCEPTION_ACCOUNT_INACTIVE);
        }

        if (user.getStatus() == EntityStatus.PENDING) {
            otpService.sendOtp(user.getEmail(), user.displayName(), OtpPurpose.ACCOUNT_ACTIVATION);
            log.info("Account activation required for pending admin '{}'", email);
            return AdminLoginResponse.builder()
                    .status(STATUS_REQUIRES_ACTIVATION)
                    .email(user.getEmail())
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
        }

        return buildLoginSuccess(user);
    }

    @Transactional
    public AdminAuthResponse verifyOtpAndGenerateToken(OtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        AdminUser user = adminUserRepository.findByEmailIgnoreCaseAndStatus(email, EntityStatus.PENDING)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ADMIN));

        otpService.verifyOtp(email, request.getOtpCode(), OtpPurpose.ACCOUNT_ACTIVATION);
        user.setStatus(EntityStatus.ACTIVE);
        user.setLastActive(Instant.now());
        adminUserRepository.save(user);

        return toAuthResponseWithNewTokens(user);
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        OtpPurpose purpose = request.getPurpose();
        String recipientName = "Admin";

        if (purpose == OtpPurpose.ACCOUNT_ACTIVATION) {
            var user = adminUserRepository.findByEmailIgnoreCaseAndStatus(email, EntityStatus.PENDING);
            if (user.isEmpty()) {
                log.info("Skipped activation OTP resend for unknown pending admin: {}", email);
                return;
            }
            recipientName = user.get().displayName();
        } else if (purpose == OtpPurpose.PASSWORD_RESET) {
            var user = adminUserRepository.findByEmailIgnoreCaseAndStatus(email, EntityStatus.ACTIVE);
            if (user.isEmpty()) {
                log.info("Skipped password reset OTP resend for unknown active admin: {}", email);
                return;
            }
            recipientName = user.get().displayName();
        }

        otpService.sendOtp(email, recipientName, purpose);
    }

    @Transactional
    public AdminAuthResponse refreshAccessToken(String refreshTokenValue) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ExpiredRefreshTokenException(EntityNames.REFRESH_TOKEN_EXPIRED,
                        MessageKeys.EXCEPTION_REFRESH_TOKEN_NOT_FOUND));

        if (refreshTokenEntity.isRevoked()) {
            log.warn("Refresh token reuse detected for admin ID: {}. Revoking all tokens.",
                    refreshTokenEntity.getUser().getId());
            refreshTokenRepository.revokeAllByUserId(refreshTokenEntity.getUser().getId());
            throw new ExpiredRefreshTokenException(EntityNames.REFRESH_TOKEN_EXPIRED,
                    MessageKeys.EXCEPTION_REFRESH_TOKEN_REUSE);
        }

        if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenEntity.setRevoked(true);
            refreshTokenRepository.save(refreshTokenEntity);
            throw new ExpiredRefreshTokenException(EntityNames.REFRESH_TOKEN_EXPIRED,
                    MessageKeys.EXCEPTION_REFRESH_TOKEN_EXPIRED);
        }

        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        String email = refreshTokenEntity.getUser().getEmail();
        AdminUser user = adminUserRepository.findByEmailIgnoreCaseAndStatus(email, EntityStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ADMIN));

        return toAuthResponseWithNewTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("Refresh token revoked for admin: {}", token.getUser().getEmail());
                });
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        var user = adminUserRepository.findByEmailIgnoreCaseAndStatus(normalizedEmail, EntityStatus.ACTIVE);
        if (user.isEmpty()) {
            log.info("Password reset requested for non-existing admin account: {}", normalizedEmail);
            return;
        }

        String code = otpService.generateAndSaveOtp(normalizedEmail, OtpPurpose.PASSWORD_RESET);
        PasswordResetTokenEntity passwordResetToken = PasswordResetTokenEntity.builder()
                .code(code)
                .email(normalizedEmail)
                .expirationTime(LocalDateTime.now().plusMinutes(securityProperties.getPasswordResetExpirationMinutes()))
                .build();
        passwordResetTokenRepository.save(passwordResetToken);
        otpService.sendOtp(normalizedEmail, user.get().displayName(), OtpPurpose.PASSWORD_RESET);
    }

    @Transactional
    public VerifyCodeResponse verifyCode(VerifyCodeRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        ensurePasswordResetNotBlocked(normalizedEmail);

        PasswordResetTokenEntity resetToken = passwordResetTokenRepository
                .findByEmailAndCode(normalizedEmail, request.getCode())
                .filter(t -> !t.getExpirationTime().isBefore(LocalDateTime.now()))
                .orElseGet(() -> {
                    registerFailedPasswordResetAttempt(normalizedEmail);
                    throw new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                            MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_PASSWORD_RESET_TOKEN);
                });

        resetToken.setVerified(true);
        passwordResetTokenRepository.save(resetToken);
        passwordResetAttemptState.remove(normalizedEmail);
        return new VerifyCodeResponse(true);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        passwordResetTokenRepository.findByEmailAndVerifiedTrue(email)
                .filter(t -> !t.getExpirationTime().isBefore(LocalDateTime.now()))
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_PASSWORD_RESET_TOKEN));

        AdminUser user = findActiveUserByEmail(email);
        validatePasswordMatch(request.getNewPassword(), request.getRetryPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(user);
        passwordResetTokenRepository.deleteAllByEmail(email);
        log.info("Password reset completed for admin '{}'", email);
    }

    @Transactional(readOnly = true)
    public CurrentAdminResponse getCurrentAdmin() {
        AdminPrincipal principal = authHelper.getAuthenticatedUser();
        AdminUser user = adminUserRepository.findByEmailIgnoreCase(principal.getEmail())
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ADMIN));

        return CurrentAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(AdminRole::getName).sorted().toList())
                .permissions(user.effectivePermissions().stream().sorted().toList())
                .build();
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int deleted = refreshTokenRepository.deleteRevokedAndExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} revoked/expired admin refresh tokens", deleted);
        }
    }

    private AdminLoginResponse buildLoginSuccess(AdminUser user) {
        user.setLastActive(Instant.now());
        adminUserRepository.save(user);

        List<String> permissions = user.effectivePermissions().stream().sorted().toList();
        List<String> roles = user.getRoles().stream().map(AdminRole::getName).sorted().toList();
        Duration ttl = Duration.ofMinutes(securityProperties.getAccessTokenTtlMinutes());
        String accessToken = mintAccessToken(user, permissions, roles, ttl);
        RefreshTokenEntity refreshToken = createAndSaveRefreshToken(user);

        log.info("Admin '{}' authenticated successfully", user.getEmail());
        return AdminLoginResponse.builder()
                .status(STATUS_SUCCESS)
                .accessToken(accessToken)
                .expiresAt(Instant.now().plus(ttl))
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .permissions(permissions)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private AdminAuthResponse toAuthResponseWithNewTokens(AdminUser user) {
        List<String> permissions = user.effectivePermissions().stream().sorted().toList();
        List<String> roles = user.getRoles().stream().map(AdminRole::getName).sorted().toList();
        Duration ttl = Duration.ofMinutes(securityProperties.getAccessTokenTtlMinutes());
        String accessToken = mintAccessToken(user, permissions, roles, ttl);
        RefreshTokenEntity refreshToken = createAndSaveRefreshToken(user);

        return AdminAuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .permissions(permissions)
                .accessToken(accessToken)
                .expiresAt(Instant.now().plus(ttl))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private RefreshTokenEntity createAndSaveRefreshToken(AdminUser user) {
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(securityProperties.getRefreshTokenValidityDays()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private String mintAccessToken(AdminUser user, List<String> permissions, List<String> roles, Duration ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("first_name", user.getFirstName());
        claims.put("last_name", user.getLastName());
        claims.put("authorities", permissions);
        claims.put("roles", roles);
        return jwtUtil.generateToken(user.getEmail(), claims, ttl);
    }

    private AdminUser findActiveUserByEmail(String email) {
        return adminUserRepository.findByEmailIgnoreCaseAndStatus(normalizeEmail(email), EntityStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ADMIN));
    }

    private void validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException(MessageKeys.EXCEPTION_PASSWORD_MISMATCH);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void ensurePasswordResetNotBlocked(String email) {
        PasswordResetAttemptState state = passwordResetAttemptState.get(email);
        if (state == null || state.blockedUntil == null) {
            return;
        }
        if (state.blockedUntil.isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException(EntityNames.PASSWORD_RESET_BLOCKED,
                    MessageKeys.EXCEPTION_PASSWORD_RESET_BLOCKED);
        }
        passwordResetAttemptState.remove(email);
    }

    private void registerFailedPasswordResetAttempt(String email) {
        PasswordResetAttemptState previous = passwordResetAttemptState.get(email);
        int failedAttempts = previous == null ? 0 : previous.failedAttempts;
        int nextAttempts = failedAttempts + 1;
        LocalDateTime blockedUntil = null;
        if (nextAttempts >= MAX_FAILED_PASSWORD_RESET_ATTEMPTS) {
            blockedUntil = LocalDateTime.now().plusMinutes(PASSWORD_RESET_LOCK_DURATION_MINUTES);
            nextAttempts = 0;
            log.warn("Password reset temporarily blocked for {}", email);
        }
        passwordResetAttemptState.put(email, new PasswordResetAttemptState(nextAttempts, blockedUntil));
    }

    private record PasswordResetAttemptState(int failedAttempts, LocalDateTime blockedUntil) {
    }
}
