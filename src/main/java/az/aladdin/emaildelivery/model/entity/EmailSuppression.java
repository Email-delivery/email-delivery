package az.aladdin.emaildelivery.model.entity;

import az.aladdin.emaildelivery.model.enums.EmailSuppressionSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "email_suppressions",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_suppression_email", columnNames = "email"))
public class EmailSuppression extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "unsubscribed_at", nullable = false)
    private Instant unsubscribedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailSuppressionSource source;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "sent_email_id")
    private Long sentEmailId;
}
