package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.OtpEntity;
import az.aladdin.emaildelivery.model.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    Optional<OtpEntity> findByCodeAndEmailAndPurposeAndUsedFalse(Integer code, String email, OtpPurpose purpose);
}
