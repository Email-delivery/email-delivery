package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.AdminMailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminMailConfigRepository extends JpaRepository<AdminMailConfig, Long> {

    Optional<AdminMailConfig> findTopByOrderByIdAsc();
}
