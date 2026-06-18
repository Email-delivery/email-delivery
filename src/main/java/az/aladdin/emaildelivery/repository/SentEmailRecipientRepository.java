package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SentEmailRecipientRepository extends JpaRepository<SentEmailRecipient, Long> {

    @Query("""
            SELECT r FROM SentEmailRecipient r
            JOIN FETCH r.sentEmail
            WHERE r.openTrackingToken = :token
            """)
    Optional<SentEmailRecipient> findByOpenTrackingToken(@Param("token") String token);

    @Query("""
            SELECT r FROM SentEmailRecipient r
            JOIN FETCH r.sentEmail
            WHERE r.unsubscribeToken = :token
            """)
    Optional<SentEmailRecipient> findByUnsubscribeToken(@Param("token") String token);

    @Query("""
            SELECT COUNT(r) FROM SentEmailRecipient r
            JOIN r.sentEmail e
            WHERE e.sentAt >= :from AND e.sentAt <= :to
            AND (:campaignId IS NULL OR e.campaignId = :campaignId)
            AND e.status NOT IN (az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.SCHEDULED)
            """)
    long countRecipientsInRange(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to,
            @Param("campaignId") Long campaignId);

    @Query("""
            SELECT COUNT(r) FROM SentEmailRecipient r
            JOIN r.sentEmail e
            WHERE e.sentAt >= :from AND e.sentAt <= :to
            AND (:campaignId IS NULL OR e.campaignId = :campaignId)
            AND r.status IN (
                az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.DELIVERED,
                az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.OPENED)
            """)
    long countDeliveredRecipientsInRange(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to,
            @Param("campaignId") Long campaignId);

    @Query("""
            SELECT COUNT(r) FROM SentEmailRecipient r
            JOIN r.sentEmail e
            WHERE e.sentAt >= :from AND e.sentAt <= :to
            AND (:campaignId IS NULL OR e.campaignId = :campaignId)
            AND r.status = az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.OPENED
            """)
    long countOpenedRecipientsInRange(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to,
            @Param("campaignId") Long campaignId);

    @Query("""
            SELECT COUNT(r) FROM SentEmailRecipient r
            JOIN r.sentEmail e
            WHERE e.sentAt >= :from AND e.sentAt <= :to
            AND (:campaignId IS NULL OR e.campaignId = :campaignId)
            AND r.status IN (
                az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.FAILED,
                az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.BOUNCED)
            """)
    long countFailedRecipientsInRange(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to,
            @Param("campaignId") Long campaignId);

    @Query("""
            SELECT COUNT(DISTINCT e.id) FROM SentEmail e
            WHERE e.sentAt >= :from AND e.sentAt <= :to
            AND (:campaignId IS NULL OR e.campaignId = :campaignId)
            AND e.status NOT IN (az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus.SCHEDULED)
            """)
    long countEmailsInRange(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to,
            @Param("campaignId") Long campaignId);
}
