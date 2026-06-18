package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByEmailAndCode(String email, String code);

    Optional<PasswordResetTokenEntity> findByEmailAndVerifiedTrue(String email);

    void deleteAllByEmail(String email);
}
