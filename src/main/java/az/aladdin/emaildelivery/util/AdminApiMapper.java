package az.aladdin.emaildelivery.util;

import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.enums.EntityStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class AdminApiMapper {

    private AdminApiMapper() {
    }

    public static String toApiRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        return roleName.trim().toLowerCase().replace("_", "");
    }

    public static String normalizeRoleFilter(String role) {
        return toApiRoleName(role);
    }

    public static String toApiStatus(EntityStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    public static String resolvePrimaryRoleName(AdminUser user) {
        return user.getRoles().stream()
                .map(AdminRole::getName)
                .min(Comparator.naturalOrder())
                .map(AdminApiMapper::toApiRoleName)
                .orElse(null);
    }

    public static List<String> toApiPermissions(Set<String> permissions) {
        return permissions.stream()
                .sorted()
                .distinct()
                .toList();
    }
}
