package az.aladdin.emaildelivery.exception;

/**
 * Keys for {@code classpath:messages*.properties}. Used with {@link org.springframework.context.MessageSource}.
 */
public final class MessageKeys {

    private MessageKeys() {
    }

    // --- Entity labels (prefix entity.) ---
    public static final String ENTITY_USER = "entity.user";
    public static final String ENTITY_REPORT = "entity.report";
    public static final String ENTITY_SERVICE = "entity.service";
    public static final String ENTITY_ADMIN = "entity.admin";
    public static final String ENTITY_ROLE = "entity.role";
    public static final String ENTITY_SUBSCRIPTION_PLAN = "entity.subscription_plan";
    public static final String ENTITY_EMAIL_CAMPAIGN = "entity.email_campaign";
    public static final String ENTITY_EMAIL_TEMPLATE = "entity.email_template";
    public static final String ENTITY_EMAIL_DRAFT = "entity.email_draft";
    public static final String ENTITY_EMAIL_SENDER_IDENTITY = "entity.email_sender_identity";
    public static final String ENTITY_SENT_EMAIL = "entity.sent_email";
    public static final String ENTITY_MAIL_CONFIG = "entity.mail_config";
    public static final String ENTITY_OTP = "entity.otp";
    public static final String ENTITY_PASSWORD_RESET_TOKEN = "entity.password_reset_token";

    // --- Common templates ---
    public static final String EXCEPTION_NOT_FOUND = "exception.not_found";
    public static final String EXCEPTION_ALREADY_EXISTS = "exception.already_exists";

    // --- Auth & session ---
    public static final String EXCEPTION_INVALID_CREDENTIALS = "exception.invalid_credentials";
    public static final String EXCEPTION_NO_AUTHENTICATED_USER = "exception.no_authenticated_user";
    public static final String EXCEPTION_UNAUTHORIZED = "exception.unauthorized";
    public static final String EXCEPTION_FORBIDDEN = "exception.forbidden";
    public static final String EXCEPTION_JWT_SECRET_TOO_SHORT = "exception.jwt_secret_too_short";
    public static final String EXCEPTION_ACCOUNT_INACTIVE = "exception.account_inactive";
    public static final String EXCEPTION_REGISTRATION_NOT_INVITED = "exception.registration_not_invited";
    public static final String EXCEPTION_PASSWORD_MISMATCH = "exception.password_mismatch";
    public static final String EXCEPTION_OTP_EXPIRED = "exception.otp_expired";
    public static final String EXCEPTION_REFRESH_TOKEN_MISSING = "exception.refresh_token_missing";
    public static final String EXCEPTION_REFRESH_TOKEN_NOT_FOUND = "exception.refresh_token_not_found";
    public static final String EXCEPTION_REFRESH_TOKEN_EXPIRED = "exception.refresh_token_expired";
    public static final String EXCEPTION_REFRESH_TOKEN_REUSE = "exception.refresh_token_reuse";
    public static final String EXCEPTION_PASSWORD_RESET_BLOCKED = "exception.password_reset_blocked";

    // --- Admin account & role management ---
    public static final String EXCEPTION_EMAIL_ALREADY_EXISTS = "exception.email_already_exists";
    public static final String EXCEPTION_ROLE_NAME_EXISTS = "exception.role_name_exists";
    public static final String EXCEPTION_NO_ROLES_ASSIGNED = "exception.no_roles_assigned";
    public static final String EXCEPTION_CANNOT_MODIFY_SELF = "exception.cannot_modify_self";
    public static final String EXCEPTION_CANNOT_DELETE_SELF = "exception.cannot_delete_self";
    public static final String EXCEPTION_SYSTEM_ROLE_PROTECTED = "exception.system_role_protected";
    public static final String EXCEPTION_ROLE_IN_USE = "exception.role_in_use";
    public static final String EXCEPTION_LAST_ADMIN = "exception.last_admin";
    public static final String EXCEPTION_PLAN_NAME_EXISTS = "exception.plan_name_exists";
    public static final String EXCEPTION_CAMPAIGN_NAME_EXISTS = "exception.campaign_name_exists";
    public static final String EXCEPTION_CAMPAIGN_NO_CONTACTS = "exception.campaign_no_contacts";
    public static final String EXCEPTION_CAMPAIGN_MISSING_SUBJECT = "exception.campaign_missing_subject";
    public static final String EXCEPTION_CAMPAIGN_MISSING_BODY = "exception.campaign_missing_body";
    public static final String EXCEPTION_TEMPLATE_NAME_EXISTS = "exception.template_name_exists";
    public static final String EXCEPTION_TEMPLATE_IN_USE = "exception.template_in_use";
    public static final String EXCEPTION_EMAIL_SCHEDULE_IN_PAST = "exception.email_schedule_in_past";
    public static final String EXCEPTION_EMAIL_NOT_SCHEDULED = "exception.email_not_scheduled";
    public static final String EXCEPTION_EMAIL_DRAFT_EMPTY = "exception.email_draft_empty";
    public static final String EXCEPTION_EMAIL_ALL_RECIPIENTS_SUPPRESSED = "exception.email_all_recipients_suppressed";
    public static final String EXCEPTION_EMAIL_CONTACT_EXISTS = "exception.email_contact_exists";
    public static final String EXCEPTION_EMAIL_RECIPIENT_LIMIT = "exception.email_recipient_limit";
    public static final String EXCEPTION_EMAIL_SUBJECT_TOO_LONG = "exception.email_subject_too_long";
    public static final String EXCEPTION_EMAIL_BODY_TOO_LONG = "exception.email_body_too_long";
    public static final String EXCEPTION_EMAIL_ATTACHMENT_LIMIT = "exception.email_attachment_limit";
    public static final String EXCEPTION_EMAIL_ATTACHMENT_TOO_LARGE = "exception.email_attachment_too_large";
    public static final String EXCEPTION_EMAIL_ATTACHMENT_INVALID = "exception.email_attachment_invalid";
    public static final String EXCEPTION_EMAIL_RESEND_NO_RECIPIENTS = "exception.email_resend_no_recipients";
    public static final String EXCEPTION_EMAIL_SENDER_EXISTS = "exception.email_sender_exists";
    public static final String EXCEPTION_MAIL_CONFIG_ALREADY_EXISTS = "exception.mail_config_already_exists";
    public static final String EXCEPTION_MAIL_CONFIG_PASSWORD_REQUIRED = "exception.mail_config_password_required";
    public static final String EXCEPTION_MAIL_CONFIG_TEST_FAILED = "exception.mail_config_test_failed";

    // --- Request validation / generic ---
    public static final String EXCEPTION_BAD_REQUEST = "exception.bad_request";
    public static final String EXCEPTION_VALIDATION_FAILED = "exception.validation_failed";
    public static final String EXCEPTION_MALFORMED_REQUEST = "exception.malformed_request";
    public static final String EXCEPTION_MISSING_PARAMETER = "exception.missing_parameter";
    public static final String EXCEPTION_INVALID_FIELD_VALUE = "exception.invalid_field_value";
    public static final String EXCEPTION_METHOD_NOT_ALLOWED = "exception.method_not_allowed";
    public static final String EXCEPTION_RESOURCE_NOT_FOUND = "exception.resource_not_found";
    public static final String EXCEPTION_ACCESS_DENIED = "exception.access_denied";
    public static final String EXCEPTION_INTERNAL_ERROR = "exception.internal_error";
    public static final String EXCEPTION_EXTERNAL_SERVICE_ERROR = "exception.external_service_error";
}
