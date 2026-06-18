package az.aladdin.emaildelivery.service.account;

import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.AdminMapper;
import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.request.account.CreateRoleRequest;
import az.aladdin.emaildelivery.model.request.account.UpdateRoleRequest;
import az.aladdin.emaildelivery.model.response.account.AdminRoleResponse;
import az.aladdin.emaildelivery.repository.AdminRoleRepository;
import az.aladdin.emaildelivery.repository.AdminUserRepository;
import az.aladdin.emaildelivery.util.Permissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Management of roles (named permission bundles) and the catalog of assignable permissions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminMapper adminMapper;

    public List<String> listPermissions() {
        return Permissions.catalog();
    }

    @Transactional(readOnly = true)
    public List<AdminRoleResponse> list() {
        return adminRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(adminMapper::toRoleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminRoleResponse get(Long id) {
        return adminMapper.toRoleResponse(findById(id));
    }

    @Transactional
    public AdminRoleResponse create(CreateRoleRequest request) {
        String name = request.getName().trim();
        if (adminRoleRepository.existsByNameIgnoreCase(name)) {
            throw new BadException(EntityNames.ROLE_NAME_EXISTS, MessageKeys.EXCEPTION_ROLE_NAME_EXISTS, name);
        }
        AdminRole role = AdminRole.builder()
                .name(name)
                .description(request.getDescription())
                .systemRole(false)
                .permissions(normalizePermissions(request.getPermissions()))
                .build();
        log.info("Created role '{}'", name);
        return adminMapper.toRoleResponse(adminRoleRepository.save(role));
    }

    @Transactional
    public AdminRoleResponse update(Long id, UpdateRoleRequest request) {
        AdminRole role = findById(id);
        ensureNotSystemRole(role);
        role.setDescription(request.getDescription());
        role.setPermissions(normalizePermissions(request.getPermissions()));
        return adminMapper.toRoleResponse(adminRoleRepository.save(role));
    }

    @Transactional
    public void delete(Long id) {
        AdminRole role = findById(id);
        ensureNotSystemRole(role);
        if (adminUserRepository.existsByRolesContaining(role)) {
            throw new BadException(EntityNames.ROLE_IN_USE, MessageKeys.EXCEPTION_ROLE_IN_USE);
        }
        adminRoleRepository.delete(role);
        log.info("Deleted role '{}'", role.getName());
    }

    private AdminRole findById(Long id) {
        return adminRoleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_ROLE));
    }

    private void ensureNotSystemRole(AdminRole role) {
        if (role.isSystemRole()) {
            throw new BadException(EntityNames.SYSTEM_ROLE_PROTECTED, MessageKeys.EXCEPTION_SYSTEM_ROLE_PROTECTED);
        }
    }

    private LinkedHashSet<String> normalizePermissions(Set<String> permissions) {
        return permissions.stream()
                .map(String::trim)
                .filter(permission -> !permission.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
