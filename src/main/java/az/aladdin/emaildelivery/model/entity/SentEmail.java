package az.aladdin.emaildelivery.model.entity;

import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sent_emails")
public class SentEmail extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(name = "body_html", nullable = false, columnDefinition = "LONGTEXT")
    private String bodyHtml;

    @Column(name = "sent_by_user_id")
    private Long sentByUserId;

    @Column(name = "sent_by_email", nullable = false, length = 160)
    private String sentByEmail;

    @Column(name = "sent_by_name", nullable = false, length = 120)
    private String sentByName;

    @Column(name = "sender_identity_id")
    private Long senderIdentityId;

    @Column(name = "from_email", length = 160)
    private String fromEmail;

    @Column(name = "from_name", length = 120)
    private String fromName;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailDeliveryStatus status = EmailDeliveryStatus.QUEUED;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "resend_of_email_id")
    private Long resendOfEmailId;

    @Builder.Default
    @Column(name = "include_unsubscribe", nullable = false)
    private boolean includeUnsubscribe = false;

    @Builder.Default
    @OneToMany(mappedBy = "sentEmail", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SentEmailRecipient> recipients = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sentEmail", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SentEmailAttachment> attachments = new ArrayList<>();
}
