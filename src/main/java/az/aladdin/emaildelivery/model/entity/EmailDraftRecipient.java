package az.aladdin.emaildelivery.model.entity;

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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_draft_recipients")
public class EmailDraftRecipient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false, foreignKey = @ForeignKey(name = "fk_email_draft_recipient_draft"))
    private EmailDraft draft;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 10)
    private EmailRecipientType recipientType;
}
