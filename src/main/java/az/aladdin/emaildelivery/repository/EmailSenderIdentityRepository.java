package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailSenderIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailSenderIdentityRepository extends JpaRepository<EmailSenderIdentity, Long> {

    List<EmailSenderIdentity> findByActiveTrueOrderByDisplayNameAsc();

    Optional<EmailSenderIdentity> findByIdAndActiveTrue(Long id);

    Optional<EmailSenderIdentity> findByDefaultSenderTrueAndActiveTrue();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Modifying
    @Query("UPDATE EmailSenderIdentity e SET e.defaultSender = false WHERE e.defaultSender = true")
    void clearDefaultSender();
}
