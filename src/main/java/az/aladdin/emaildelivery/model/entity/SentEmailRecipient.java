package az.aladdin.emaildelivery.model.entity;

import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import az.aladdin.emaildelivery.model.enums.EmailRecipientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sent_email_recipients")
public class SentEmailRecipient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sent_email_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sent_email_recipient_email"))
    private SentEmail sentEmail;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 10)
    private EmailRecipientType recipientType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailDeliveryStatus status = EmailDeliveryStatus.QUEUED;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "open_tracking_token", length = 36, unique = true)
    private String openTrackingToken;

    @Column(name = "unsubscribe_token", length = 36, unique = true)
    private String unsubscribeToken;

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;
}
