package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.SentEmail;
import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {

    List<SentEmail> findByStatusAndScheduledAtLessThanEqual(EmailDeliveryStatus status, Instant scheduledAt);

    @Query("""
            SELECT DISTINCT e FROM SentEmail e
            LEFT JOIN FETCH e.recipients
            ORDER BY e.sentAt DESC
            """)
    List<SentEmail> findAllWithRecipients();

    @Query("""
            SELECT e FROM SentEmail e
            LEFT JOIN FETCH e.recipients
            WHERE e.id = :id
            """)
    Optional<SentEmail> findByIdWithRecipients(Long id);
}
