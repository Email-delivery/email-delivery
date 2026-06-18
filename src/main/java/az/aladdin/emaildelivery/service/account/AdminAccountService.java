package az.aladdin.emaildelivery.service.account;

import az.aladdin.emaildelivery.config.security.AdminPrincipal;
import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.AdminMapper;
import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.enums.EntityStatus;
import az.aladdin.emaildelivery.model.request.account.AssignRolesRequest;
import az.aladdin.emaildelivery.model.request.account.CreateAdminRequest;
import az.aladdin.emaildelivery.model.request.account.UpdateAdminRequest;
import az.aladdin.emaildelivery.model.response.account.AdminAccountPageResponse;
import az.aladdin.emaildelivery.model.response.account.AdminAccountResponse;
import az.aladdin.emaildelivery.repository.AdminRoleRepository;
import az.aladdin.emaildelivery.repository.AdminUserRepository;
import az.aladdin.emaildelivery.util.AdminApiMapper;
import az.aladdin.emaildelivery.util.AuthHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * CRUD and lifecycle management for the admin panel's own user accounts. All state lives in this service's database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminMapper adminMapper;
    private final AuthHelper authHelper;

    @Transactional(readOnly = true)
    public AdminAccountPageResponse list(String search, String role, String status, int page, int limit) {
        EntityStatus statusFilter = parseStatus(status);
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        String normalizedRole = AdminApiMapper.normalizeRoleFilter(role);
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedLimit = limit < 1 ? 20 : limit;

        Page<AdminUser> result = adminUserRepository.search(
                normalizedSearch,
                statusFilter,
                normalizedRole,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "createdAt")));

        return AdminAccountPageResponse.builder()
                .items(result.getContent().stream().map(adminMapper::toAccountResponse).toList())
                .total(result.getTotalElements())
                .page(normalizedPage)
                .limit(normalizedLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminAccountResponse get(Long id) {
        return adminMapper.toAccountResponse(findById(id));
    }

    @Transactional
    public AdminAccountResponse create(CreateAdminRequest request) {
        String email = request.getEmail().trim();
        if (adminUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BadException(EntityNames.EMAIL_ALREADY_EXISTS, MessageKeys.EXCEPTION_EMAIL_ALREADY_EXISTS, email);
        }
        AdminUser user = AdminUser.builder()
                .email(email)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .password(resolveInitialPassword(null))
                .status(parseRequiredStatus(request.getStatus()))
                .roles(resolveRoleByName(request.getRole()))
                .build();
        user = adminUserRepository.save(user);
        log.info("Created admin account '{}'", email);
        return adminMapper.toAccountResponse(user);
    }

    @Transactional
    public AdminAccountResponse update(Long id, UpdateAdminRequest request) {
        AdminUser user = findById(id);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setStatus(parseRequiredStatus(request.getStatus()));
        user.setRoles(resolveRoleByName(request.getRole()));
        return adminMapper.toAccountResponse(adminUserRepository.save(user));
    }

    @Transactional
    public AdminAccountResponse assignRoles(Long id, AssignRolesRequest request) {
        AdminUser user = findById(id);
        user.setRoles(resolveRoles(request.getRoleIds()));
        return adminMapper.toAccountResponse(adminUserRepository.save(user));
    }

    @Transactional
    public AdminAccountResponse activate(Long id) {
        return changeStatus(id, EntityStatus.ACTIVE);
    }

    @Transactional
    public AdminAccountResponse deactivate(Long id) {
        AdminUser user = findById(id);
        ensureNotSelf(user, EntityNames.CANNOT_MODIFY_SELF, MessageKeys.EXCEPTION_CANNOT_MODIFY_SELF);
        ensureNotLastActiveAdmin(user);
        user.setStatus(EntityStatus.INACTIVE);
        return adminMapper.toAccountResponse(adminUserRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        AdminUser user = findById(id);
        ensureNotSelf(user, EntityNames.CANNOT_DELETE_SELF, MessageKeys.EXCEPTION_CANNOT_DELETE_SELF);
        ensureNotLastActiveAdmin(user);
        adminUserRepository.delete(user);
        log.info("Deleted admin account '{}'", user.getEmail());
    }

    private AdminAccountResponse changeStatus(Long id, EntityStatus status) {
        AdminUser user = findById(id);
        user.setStatus(status);
        return adminMapper.toAccountResponse(adminUserRepository.save(user));
    }

    private AdminUser findById(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ADMIN));
    }

    private Set<AdminRole> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadException(EntityNames.NO_ROLES_ASSIGNED, MessageKeys.EXCEPTION_NO_ROLES_ASSIGNED);
        }
        Set<AdminRole> roles = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            roles.add(adminRoleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                            MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ROLE)));
        }
        return roles;
    }

    private void ensureNotSelf(AdminUser user, String code, String messageKey) {
        AdminPrincipal current = authHelper.getAuthenticatedUser();
        if (current.getUserId() != null && current.getUserId().equals(user.getId())) {
            throw new BadException(code, messageKey);
        }
    }

    private void ensureNotLastActiveAdmin(AdminUser user) {
        if (user.isActive() && adminUserRepository.countByStatus(EntityStatus.ACTIVE) <= 1) {
            throw new BadException(EntityNames.LAST_ADMIN, MessageKeys.EXCEPTION_LAST_ADMIN);
        }
    }

    private EntityStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return EntityStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadException(EntityNames.INVALID_FIELD_VALUE, MessageKeys.EXCEPTION_INVALID_FIELD_VALUE);
        }
    }

    private EntityStatus parseRequiredStatus(String status) {
        EntityStatus parsed = parseStatus(status);
        if (parsed == null) {
            throw new BadException(EntityNames.INVALID_FIELD_VALUE, MessageKeys.EXCEPTION_INVALID_FIELD_VALUE);
        }
        return parsed;
    }

    private Set<AdminRole> resolveRoleByName(String roleName) {
        AdminRole role = adminRoleRepository.findByNameIgnoreCase(roleName.trim())
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ROLE));
        return new LinkedHashSet<>(Set.of(role));
    }

    private String resolveInitialPassword(String password) {
        if (password == null || password.isBlank()) {
            return passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return passwordEncoder.encode(password);
    }
}
