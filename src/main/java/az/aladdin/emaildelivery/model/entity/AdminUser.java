package az.aladdin.emaildelivery.model.entity;

import az.aladdin.emaildelivery.model.enums.EntityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A person who can sign in to the admin panel. Authentication is fully local — credentials live in this service's
 * own database and have no relationship to PMS/RMS user accounts.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_users", uniqueConstraints = @UniqueConstraint(name = "uk_admin_user_email", columnNames = "email"))
public class AdminUser extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "last_active")
    private Instant lastActive;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_user_roles",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_role_user")),
            inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role_role")))
    private Set<AdminRole> roles = new LinkedHashSet<>();

    public boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }

    public String displayName() {
        return firstName.trim() + " " + lastName.trim();
    }

    /**
     * Effective authorities = union of every permission granted by every assigned role.
     */
    public Set<String> effectivePermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
