package az.aladdin.emaildelivery.bootstrap;

import az.aladdin.emaildelivery.config.AdminBootstrapProperties;
import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.enums.EntityStatus;
import az.aladdin.emaildelivery.util.Permissions;
import az.aladdin.emaildelivery.repository.AdminRoleRepository;
import az.aladdin.emaildelivery.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Seeds the built-in SUPER_ADMIN role (always kept in sync with the full permission catalog) and the configured
 * bootstrap super-admin account from {@code admin.bootstrap.*}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements ApplicationRunner {

    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private final AdminRoleRepository adminRoleRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties bootstrapProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AdminRole superAdmin = ensureSuperAdminRole();
        ensureBootstrapAdmin(superAdmin);
    }

    private AdminRole ensureSuperAdminRole() {
        AdminRole role = adminRoleRepository.findByNameIgnoreCase(SUPER_ADMIN_ROLE)
                .orElseGet(() -> AdminRole.builder()
                        .name(SUPER_ADMIN_ROLE)
                        .description("Full access to every admin-panel capability")
                        .systemRole(true)
                        .build());
        role.setSystemRole(true);
        role.getPermissions().clear();
        role.getPermissions().addAll(Permissions.all());
        return adminRoleRepository.save(role);
    }

    private void ensureBootstrapAdmin(AdminRole superAdmin) {
        if (!bootstrapProperties.isEnabled()) {
            log.info("Bootstrap admin provisioning is disabled");
            return;
        }

        String email = normalizeEmail(bootstrapProperties.getEmail());
        String encodedPassword = passwordEncoder.encode(bootstrapProperties.getPassword());

        adminUserRepository.findByEmailIgnoreCase(email).ifPresentOrElse(
                existing -> syncBootstrapAdmin(existing, superAdmin, encodedPassword, email),
                () -> createBootstrapAdmin(superAdmin, email, encodedPassword));
    }

    private void syncBootstrapAdmin(AdminUser existing, AdminRole superAdmin, String encodedPassword, String email) {
        existing.setPassword(encodedPassword);
        existing.setStatus(EntityStatus.ACTIVE);
        existing.setFirstName(bootstrapProperties.getFirstName().trim());
        existing.setLastName(bootstrapProperties.getLastName().trim());
        existing.getRoles().clear();
        existing.getRoles().add(superAdmin);
        adminUserRepository.save(existing);
        log.info("Bootstrap admin '{}' synced from configuration (password refreshed, SUPER_ADMIN role assigned)", email);
    }

    private void createBootstrapAdmin(AdminRole superAdmin, String email, String encodedPassword) {
        AdminUser admin = AdminUser.builder()
                .email(email)
                .firstName(bootstrapProperties.getFirstName().trim())
                .lastName(bootstrapProperties.getLastName().trim())
                .password(encodedPassword)
                .status(EntityStatus.ACTIVE)
                .roles(new LinkedHashSet<>(Set.of(superAdmin)))
                .build();
        adminUserRepository.save(admin);
        log.warn("Bootstrapped super-admin '{}'. Change this password immediately after first login.", email);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
