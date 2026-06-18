package az.aladdin.emaildelivery.config.security;

import az.aladdin.emaildelivery.util.Permissions;
import lombok.Getter;
import org.springframework.http.HttpMethod;

/**
 * Central registry of every admin-panel route and the permission string(s) that may reach it. A request is allowed
 * when the caller holds <em>any</em> of the listed permissions. Public routes carry no permissions.
 */
@Getter
public enum ApiEndpoint {

    // --- Public ---
    SWAGGER_V3_ALL("/v3/api-docs/**", null),
    SWAGGER_UI("/swagger-ui/**", null),
    SWAGGER_UI_HTML("/swagger-ui.html", null),
    ERROR_ENDPOINT("/error", null),

    AUTH_SIGN_IN("/admin/v1/auth/sign-in", HttpMethod.POST),
    AUTH_LOGIN("/admin/v1/auth/login", HttpMethod.POST),
    AUTH_SIGN_UP("/admin/v1/auth/sign-up", HttpMethod.POST),
    AUTH_VERIFY_OTP("/admin/v1/auth/verify-otp", HttpMethod.POST),
    AUTH_RESEND_OTP("/admin/v1/auth/resend-otp", HttpMethod.POST),
    AUTH_REFRESH("/admin/v1/auth/refresh", HttpMethod.POST),
    AUTH_FORGOT_PASSWORD("/admin/v1/auth/forgot-password", HttpMethod.POST),
    AUTH_VERIFY_CODE("/admin/v1/auth/verify-code", HttpMethod.POST),
    AUTH_RESET_PASSWORD("/admin/v1/auth/reset-password", HttpMethod.PATCH),

    PUBLIC_EMAIL_OPEN("/public/v1/emails/open/*", HttpMethod.GET),
    PUBLIC_EMAIL_UNSUBSCRIBE_GET("/public/v1/emails/unsubscribe/*", HttpMethod.GET),
    PUBLIC_EMAIL_UNSUBSCRIBE_POST("/public/v1/emails/unsubscribe/*", HttpMethod.POST),

    // --- Current admin (any authenticated admin) ---
    AUTH_ME("/admin/v1/auth/me", HttpMethod.GET),
    AUTH_SIGN_OUT("/admin/v1/auth/sign-out", HttpMethod.POST),
    AUTH_LOGOUT("/admin/v1/auth/logout", HttpMethod.POST),

    // --- Panel admin account management ---
    ACCOUNTS_LIST("/admin/v1/accounts", HttpMethod.GET, Permissions.PANEL_USERS_READ),
    ACCOUNTS_GET("/admin/v1/accounts/{id}", HttpMethod.GET, Permissions.PANEL_USERS_READ),
    ACCOUNTS_CREATE("/admin/v1/accounts", HttpMethod.POST, Permissions.PANEL_USERS_WRITE),
    ACCOUNTS_UPDATE("/admin/v1/accounts/{id}", HttpMethod.PUT, Permissions.PANEL_USERS_WRITE),
    ACCOUNTS_ACTIVATE("/admin/v1/accounts/{id}/activate", HttpMethod.PATCH, Permissions.PANEL_USERS_WRITE),
    ACCOUNTS_DEACTIVATE("/admin/v1/accounts/{id}/deactivate", HttpMethod.PATCH, Permissions.PANEL_USERS_WRITE),
    ACCOUNTS_ROLES("/admin/v1/accounts/{id}/roles", HttpMethod.PUT, Permissions.PANEL_USERS_WRITE),
    ACCOUNTS_DELETE("/admin/v1/accounts/{id}", HttpMethod.DELETE, Permissions.PANEL_USERS_WRITE),

    // --- Role & permission management ---
    PERMISSIONS_LIST("/admin/v1/permissions", HttpMethod.GET, Permissions.PANEL_USERS_READ),
    ROLES_LIST("/admin/v1/roles", HttpMethod.GET, Permissions.PANEL_USERS_READ),
    ROLES_GET("/admin/v1/roles/{id}", HttpMethod.GET, Permissions.PANEL_USERS_READ),
    ROLES_CREATE("/admin/v1/roles", HttpMethod.POST, Permissions.PANEL_USERS_WRITE),
    ROLES_UPDATE("/admin/v1/roles/{id}", HttpMethod.PUT, Permissions.PANEL_USERS_WRITE),
    ROLES_DELETE("/admin/v1/roles/{id}", HttpMethod.DELETE, Permissions.PANEL_USERS_WRITE),

    // --- Email campaigns & sending ---
    CAMPAIGNS_LIST("/admin/v1/campaigns", HttpMethod.GET, Permissions.EMAILS_READ),
    CAMPAIGNS_GET("/admin/v1/campaigns/{id}", HttpMethod.GET, Permissions.EMAILS_READ),
    CAMPAIGNS_CREATE("/admin/v1/campaigns", HttpMethod.POST, Permissions.EMAILS_WRITE),
    CAMPAIGNS_UPDATE("/admin/v1/campaigns/{id}", HttpMethod.PUT, Permissions.EMAILS_WRITE),
    CAMPAIGNS_DELETE("/admin/v1/campaigns/{id}", HttpMethod.DELETE, Permissions.EMAILS_WRITE),
    CAMPAIGNS_SEND("/admin/v1/campaigns/{id}/send", HttpMethod.POST, Permissions.EMAILS_WRITE),
    EMAIL_TEMPLATES_LIST("/admin/v1/email-templates", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_TEMPLATES_GET("/admin/v1/email-templates/{id}", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_TEMPLATES_CREATE("/admin/v1/email-templates", HttpMethod.POST, Permissions.EMAILS_WRITE),
    EMAIL_TEMPLATES_UPDATE("/admin/v1/email-templates/{id}", HttpMethod.PUT, Permissions.EMAILS_WRITE),
    EMAIL_TEMPLATES_DELETE("/admin/v1/email-templates/{id}", HttpMethod.DELETE, Permissions.EMAILS_WRITE),
    EMAIL_CONTACTS_LIST("/admin/v1/email-contacts", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_CONTACTS_GET("/admin/v1/email-contacts/{id}", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_CONTACTS_CREATE("/admin/v1/email-contacts", HttpMethod.POST, Permissions.EMAILS_WRITE),
    EMAIL_CONTACTS_UPDATE("/admin/v1/email-contacts/{id}", HttpMethod.PUT, Permissions.EMAILS_WRITE),
    EMAIL_CONTACTS_DELETE("/admin/v1/email-contacts/{id}", HttpMethod.DELETE, Permissions.EMAILS_WRITE),
    EMAILS_SEND("/admin/v1/emails/send", HttpMethod.POST, Permissions.EMAILS_WRITE),
    EMAILS_RESEND("/admin/v1/emails/*/resend", HttpMethod.POST, Permissions.EMAILS_WRITE),
    EMAILS_SCHEDULE_UPDATE("/admin/v1/emails/*/schedule", HttpMethod.PATCH, Permissions.EMAILS_WRITE),
    EMAILS_HISTORY("/admin/v1/emails/history", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_SENDER_IDENTITIES_LIST("/admin/v1/email-sender-identities", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_SENDER_IDENTITIES_LIST_ALL("/admin/v1/email-sender-identities/all", HttpMethod.GET, Permissions.MAIL_CONFIG_WRITE),
    EMAIL_SENDER_IDENTITIES_CREATE("/admin/v1/email-sender-identities", HttpMethod.POST, Permissions.MAIL_CONFIG_WRITE),
    EMAIL_SENDER_IDENTITIES_UPDATE("/admin/v1/email-sender-identities/{id}", HttpMethod.PUT, Permissions.MAIL_CONFIG_WRITE),
    EMAIL_SENDER_IDENTITIES_DELETE("/admin/v1/email-sender-identities/{id}", HttpMethod.DELETE, Permissions.MAIL_CONFIG_WRITE),
    EMAILS_ANALYTICS("/admin/v1/emails/analytics", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_SUPPRESSIONS_LIST("/admin/v1/email-suppressions", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_SUPPRESSIONS_DELETE("/admin/v1/email-suppressions/{id}", HttpMethod.DELETE, Permissions.EMAILS_WRITE),
    EMAIL_DRAFTS_GET("/admin/v1/email-drafts/current", HttpMethod.GET, Permissions.EMAILS_READ),
    EMAIL_DRAFTS_SAVE("/admin/v1/email-drafts/current", HttpMethod.PUT, Permissions.EMAILS_WRITE),
    EMAIL_DRAFTS_DELETE("/admin/v1/email-drafts/current", HttpMethod.DELETE, Permissions.EMAILS_WRITE),

    MAIL_CONFIG_GET("/admin/v1/settings/mail-config", HttpMethod.GET, Permissions.MAIL_CONFIG_READ),
    MAIL_CONFIG_CREATE("/admin/v1/settings/mail-config", HttpMethod.POST, Permissions.MAIL_CONFIG_WRITE),
    MAIL_CONFIG_UPDATE("/admin/v1/settings/mail-config", HttpMethod.PUT, Permissions.MAIL_CONFIG_WRITE),
    MAIL_CONFIG_TEST("/admin/v1/settings/mail-config/test-email", HttpMethod.POST, Permissions.MAIL_CONFIG_WRITE);

    private final String pathPattern;
    private final HttpMethod httpMethod;
    private final String[] permissions;

    ApiEndpoint(String pathPattern, HttpMethod httpMethod, String... permissions) {
        this.pathPattern = normalizePathPattern(pathPattern);
        this.httpMethod = httpMethod;
        this.permissions = permissions;
    }

    public boolean isPublic() {
        return this == SWAGGER_V3_ALL || this == SWAGGER_UI || this == SWAGGER_UI_HTML
                || this == ERROR_ENDPOINT
                || this == AUTH_SIGN_IN || this == AUTH_LOGIN || this == AUTH_SIGN_UP
                || this == AUTH_VERIFY_OTP || this == AUTH_RESEND_OTP || this == AUTH_REFRESH
                || this == AUTH_SIGN_OUT || this == AUTH_LOGOUT
                || this == AUTH_FORGOT_PASSWORD || this == AUTH_VERIFY_CODE || this == AUTH_RESET_PASSWORD
                || this == PUBLIC_EMAIL_OPEN
                || this == PUBLIC_EMAIL_UNSUBSCRIBE_GET
                || this == PUBLIC_EMAIL_UNSUBSCRIBE_POST;
    }

    public boolean isAuthenticatedOnly() {
        return permissions.length == 0 && !isPublic();
    }

    private static String normalizePathPattern(String pathPattern) {
        return pathPattern.replaceAll("\\{[^/]+}", "*");
    }
}
