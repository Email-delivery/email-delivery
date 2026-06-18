package az.aladdin.emaildelivery.util;

import java.time.Instant;

public final class InstantFormatting {

    private InstantFormatting() {
    }

    /** ISO-8601 UTC string for API responses; clients format in the user's timezone. */
    public static String toApiString(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
