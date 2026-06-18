package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.response.account.AdminAccountResponse;
import az.aladdin.emaildelivery.model.response.account.AdminRoleResponse;
import az.aladdin.emaildelivery.util.AdminApiMapper;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

/**
 * Converts persistent admin entities into their API response representations.
 */
@Component
public class AdminMapper {

    public AdminRoleResponse toRoleResponse(AdminRole role) {
        return AdminRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .permissions(role.getPermissions().stream().sorted().toList())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public AdminAccountResponse toAccountResponse(AdminUser user) {
        return AdminAccountResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(AdminApiMapper.resolvePrimaryRoleName(user))
                .status(AdminApiMapper.toApiStatus(user.getStatus()))
                .permissions(AdminApiMapper.toApiPermissions(user.effectivePermissions()))
                .lastActive(user.getLastActive())
                .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate())
                .build();
    }
}
