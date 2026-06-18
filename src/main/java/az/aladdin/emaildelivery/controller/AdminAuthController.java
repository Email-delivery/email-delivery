package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.UnauthorizedException;
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
import az.aladdin.emaildelivery.service.auth.AdminAuthService;
import az.aladdin.emaildelivery.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("admin/v1/auth")
@RequiredArgsConstructor
@Validated
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final CookieUtil cookieUtil;

    @PostMapping({"/login", "/sign-in"})
    public AdminLoginResponse login(@RequestBody @Valid AdminLoginRequest request, HttpServletResponse response) {
        AdminLoginResponse loginResponse = adminAuthService.login(request);
        if ("SUCCESS".equals(loginResponse.getStatus()) && loginResponse.getRefreshToken() != null) {
            cookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken());
        }
        return loginResponse;
    }

    @PostMapping("/sign-up")
    @ResponseStatus(NO_CONTENT)
    public void register(@RequestBody @Valid RegisterRequest request) {
        adminAuthService.register(request);
    }

    @PostMapping("/verify-otp")
    public AdminAuthResponse verifyOtp(@RequestBody @Valid OtpRequest request, HttpServletResponse response) {
        AdminAuthResponse authResponse = adminAuthService.verifyOtpAndGenerateToken(request);
        cookieUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @PostMapping("/resend-otp")
    @ResponseStatus(NO_CONTENT)
    public void resendOtp(@RequestBody @Valid ResendOtpRequest request) {
        adminAuthService.resendOtp(request);
    }

    @PostMapping("/refresh")
    public AdminAuthResponse refresh(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException(EntityNames.REFRESH_TOKEN_EXPIRED,
                    MessageKeys.EXCEPTION_REFRESH_TOKEN_MISSING);
        }
        AdminAuthResponse authResponse = adminAuthService.refreshAccessToken(refreshToken);
        cookieUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @PostMapping({"/sign-out", "/logout"})
    @ResponseStatus(NO_CONTENT)
    public void logout(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            adminAuthService.logout(refreshToken);
        }
        cookieUtil.clearRefreshTokenCookie(response);
    }

    @GetMapping("/me")
    public CurrentAdminResponse getCurrentAdmin() {
        return adminAuthService.getCurrentAdmin();
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(NO_CONTENT)
    public void forgotPassword(
            @RequestParam
            @NotBlank(message = "{validation.email.required}")
            @Size(max = 160, message = "{validation.email.too_long}")
            @Email(message = "{validation.email.invalid}")
            String email) {
        adminAuthService.requestPasswordReset(email);
    }

    @PostMapping("/verify-code")
    public VerifyCodeResponse verifyCode(@RequestBody @Valid VerifyCodeRequest request) {
        return adminAuthService.verifyCode(request);
    }

    @PatchMapping("/reset-password")
    @ResponseStatus(NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        adminAuthService.resetPassword(request);
    }
}
