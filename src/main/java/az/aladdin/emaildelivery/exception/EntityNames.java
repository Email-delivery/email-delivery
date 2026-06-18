package az.aladdin.emaildelivery.exception;

/**
 * Stable, machine-readable {@code code} values returned in {@link ApiErrorResponse}.
 */
public final class EntityNames {

    private EntityNames() {
    }

    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String NOT_FOUND_RESOURCE = "NOT_FOUND";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_INACTIVE = "ACCOUNT_INACTIVE";
    public static final String REGISTRATION_NOT_INVITED = "REGISTRATION_NOT_INVITED";
    public static final String REFRESH_TOKEN_EXPIRED = "REFRESH_TOKEN_EXPIRED";
    public static final String PASSWORD_RESET_BLOCKED = "PASSWORD_RESET_BLOCKED";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String ROLE_NAME_EXISTS = "ROLE_NAME_EXISTS";
    public static final String NO_ROLES_ASSIGNED = "NO_ROLES_ASSIGNED";
    public static final String CANNOT_MODIFY_SELF = "CANNOT_MODIFY_SELF";
    public static final String CANNOT_DELETE_SELF = "CANNOT_DELETE_SELF";
    public static final String SYSTEM_ROLE_PROTECTED = "SYSTEM_ROLE_PROTECTED";
    public static final String ROLE_IN_USE = "ROLE_IN_USE";
    public static final String LAST_ADMIN = "LAST_ADMIN";
    public static final String PLAN_NAME_EXISTS = "PLAN_NAME_EXISTS";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    public static final String INVALID_FIELD_VALUE = "INVALID_FIELD_VALUE";
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String EMAIL_CAMPAIGN = "EMAIL_CAMPAIGN";
    public static final String EMAIL_TEMPLATE = "EMAIL_TEMPLATE";
    public static final String EMAIL_DRAFT = "EMAIL_DRAFT";
    public static final String EMAIL_SUPPRESSION = "EMAIL_SUPPRESSION";
    public static final String EMAIL_ADDRESS_BOOK_CONTACT = "EMAIL_ADDRESS_BOOK_CONTACT";
    public static final String EMAIL_SENDER_IDENTITY = "EMAIL_SENDER_IDENTITY";
    public static final String SENT_EMAIL = "SENT_EMAIL";
    public static final String MAIL_CONFIG = "MAIL_CONFIG";
}
