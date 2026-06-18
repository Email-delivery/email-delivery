package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.AdminRole;
import az.aladdin.emaildelivery.model.entity.AdminUser;
import az.aladdin.emaildelivery.model.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmailIgnoreCase(String email);

    Optional<AdminUser> findByEmailIgnoreCaseAndStatus(String email, EntityStatus status);

    boolean existsByEmailIgnoreCase(String email);

    long countByStatus(EntityStatus status);

    boolean existsByRolesContaining(AdminRole role);

    @Query("""
            select distinct u from AdminUser u left join u.roles r
            where (:search is null or lower(u.email) like lower(concat('%', :search, '%'))
                   or lower(u.firstName) like lower(concat('%', :search, '%'))
                   or lower(u.lastName) like lower(concat('%', :search, '%')))
              and (:status is null or u.status = :status)
              and (:role is null or lower(function('replace', r.name, '_', '')) = :role)
            """)
    Page<AdminUser> search(@Param("search") String search,
                           @Param("status") EntityStatus status,
                           @Param("role") String role,
                           Pageable pageable);
}
