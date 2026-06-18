package az.aladdin.emaildelivery.email;

public record EmailFromAddress(String email, String displayName) {

    public boolean hasDisplayName() {
        return displayName != null && !displayName.isBlank();
    }

    public static String formatLabel(String displayName, String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim() + " <" + email.trim() + ">";
        }
        return email.trim();
    }
}
