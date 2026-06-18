package az.aladdin.emaildelivery.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission strings aligned with the email-delivery UI.
 */
public final class Permissions {

    public static final String EMAILS_READ = "emails:read";
    public static final String EMAILS_WRITE = "emails:write";

    public static final String PANEL_USERS_READ = "panel-users:read";
    public static final String PANEL_USERS_WRITE = "panel-users:write";

    public static final String SETTINGS_READ = "settings:read";
    public static final String SETTINGS_WRITE = "settings:write";

    public static final String MAIL_CONFIG_READ = "mail-config:read";
    public static final String MAIL_CONFIG_WRITE = "mail-config:write";

    private Permissions() {
    }

    public static Set<String> all() {
        return new LinkedHashSet<>(catalog());
    }

    public static List<String> catalog() {
        return List.of(
                EMAILS_READ,
                EMAILS_WRITE,
                PANEL_USERS_READ,
                PANEL_USERS_WRITE,
                SETTINGS_READ,
                SETTINGS_WRITE,
                MAIL_CONFIG_READ,
                MAIL_CONFIG_WRITE
        );
    }
}
