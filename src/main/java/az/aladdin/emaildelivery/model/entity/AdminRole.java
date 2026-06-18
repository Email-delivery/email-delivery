package az.aladdin.emaildelivery.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A named bundle of permission strings. System roles (e.g. SUPER_ADMIN) are provisioned at startup and cannot
 * be deleted; custom roles can be freely created and edited by sufficiently privileged admins.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_roles", uniqueConstraints = @UniqueConstraint(name = "uk_admin_role_name", columnNames = "name"))
public class AdminRole extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "admin_role_permissions",
            joinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_permission_role")))
    @Column(name = "permission", nullable = false, length = 64)
    private Set<String> permissions = new LinkedHashSet<>();
}
