package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

    Optional<AdminRole> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
